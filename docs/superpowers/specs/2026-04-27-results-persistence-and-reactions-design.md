# Results Persistence, Personality Re-take, and Reactions

**Date:** 2026-04-27
**App:** NextFlix (Android, Kotlin/Compose)

## Goals

1. Persist movie and book recommendation lists across app sessions so the Results tab is populated on cold start.
2. Allow the user to re-take the personality quiz at any time, overwriting their stored answers.
3. Add like/dislike reactions on recommended movies and books, both on result cards and detail screens.
4. Use stored reactions to (a) hide disliked items from future Results, and (b) steer the AI toward items similar to liked entries and away from items similar to disliked entries.

## Non-goals

- Per-content-type tracking of "personality changed since last recs". A single app-wide flag is used (cleared by either movie OR book regeneration).
- Changing the AI provider or model. The current `nvidia/nemotron-3-super-120b-a12b:free` model on OpenRouter is confirmed working and is left unchanged.
- Reaction history / undo log. Only the current state per item is stored.

## Architecture

Three new DataStore-backed stores; small additions to existing ViewModels and the recommendation service.

```
PersonalityQuizStore (existing) ──► PersonalityQuizViewModel ──► HomeTab "Retake" button
        │  + personalityChangedSinceLastRecs flag
        ▼
RecommendationResultsStore (new) ◄─► MovieRecommendationViewModel ──┐
                                  ◄─► BookRecommendationViewModel ──┤
                                                                    ▼
ReactionStore (new) ◄─► ReactionViewModel ─► RecommendationViewModel (Results tab)
                                              - filters disliked
                                              - exposes reaction state per item
                                          ─► RecommendationService.getXxxRecommendations(reactionContext)
```

## Section 1: Persisting recommendations

### `RecommendationResultsStore`

New file: `data/recommendation/RecommendationResultsStore.kt`.

- DataStore name: `"recommendation_results"`.
- Two string preference keys: `movies_json`, `books_json`.
- Methods:
  - `suspend fun readMovies(): List<Movie>`
  - `suspend fun writeMovies(movies: List<Movie>)`
  - `suspend fun readBooks(): List<Book>`
  - `suspend fun writeBooks(books: List<Book>)`
- Serialization mirrors `SavedMoviesStore` (kotlinx.serialization, `ListSerializer(Movie.serializer())` / `ListSerializer(Book.serializer())`).

### Wiring

- `MovieRecommendationViewModel.init`:
  - Read persisted recommendations; seed `_recommendations.value` (re-applying `isSaved` from `_savedMovies`).
- After every successful `generateRecommendations()` (movie VM) and the book equivalent:
  - `recommendationResultsStore.writeMovies(rankedMovies)` (or `writeBooks`).
- The Results tab's existing `RecommendationViewModel` continues to mirror movie/book VM lists via `setMovieResults` / `setBookResults` in `MainActivity`'s `LaunchedEffect`. No changes needed there beyond what Section 4 adds.

## Section 2: Re-taking the personality quiz

### Home-tab entry point

In `MainActivity.HomeTabContent`:
- Add a `Button("Retake personality quiz")` directly under the "Your personality quiz is saved..." text block (only shown when `quizResult != null`).
- The button sets a state flag (e.g. `showRetakeQuiz: Boolean`) which causes `NextFlixApp` to render `PersonalityQuizScreen` in place of the tab content (similar to how `showBookRecommendations`/`showMovieRecommendations` work today).
- `PersonalityQuizScreen` is invoked with the existing saved answers pre-loaded so the user can edit rather than start blank.
- On submit, the screen calls `personalityQuizViewModel.save(...)` (existing path) and returns to the Home tab.

### Edit-mode pre-fill

`PersonalityQuizViewModel` exposes the current saved result via `lastResult: StateFlow<PersonalityQuizResult?>`. Pass this into `PersonalityQuizScreen` as an optional initial state. If null, behaves as today (blank); if non-null, prefill answer selections.

### "Personality changed since last recs" flag

Add a second preference key to `PersonalityQuizStore`'s DataStore: `personality_changed_since_last_recs` (boolean).

- Set to `true` whenever `PersonalityQuizStore.write(...)` is called (i.e. on every save, including the initial one — which is fine, since there are no prior recommendations on initial save).
- Set to `false` after a successful `MovieRecommendationViewModel.generateRecommendations` OR a successful `BookRecommendationViewModel.generateRecommendations`. App-wide clear: re-running either quiz dismisses the banner globally.
- Exposed via `PersonalityQuizViewModel.personalityChangedSinceLastRecs: StateFlow<Boolean>`.

### Banner

In `RecommendationResultsScreen`, above the FilterChip row:
- When `personalityChangedSinceLastRecs == true` AND `items.isNotEmpty()`, render a dismissible banner:
  *"Your personality changed — re-run the movie or book quiz to refresh recommendations."*
- Dismissal is local to the composable (a `rememberSaveable` boolean) — it does not clear the underlying flag. The flag persists until a regeneration clears it.

The banner's source flag is read from a passed-in callback / state hoisted up to `MainActivity` so the screen stays UI-only.

## Section 3: Reactions (likes / dislikes)

### Data model

New file: `data/reaction/ReactionModels.kt`.

```kotlin
enum class Reaction { LIKED, DISLIKED }

@Serializable
data class ReactionEntry(
    val id: String,
    val contentType: RecommendationContentType,
    val title: String,
    val descriptor: String   // movies: comma-joined genres; books: author
)
```

### `ReactionStore`

New file: `data/reaction/ReactionStore.kt`. DataStore name `"reactions"`. Two keys:
- `liked_json`: `List<ReactionEntry>`
- `disliked_json`: `List<ReactionEntry>`

Methods:
- `suspend fun read(): ReactionState` (where `ReactionState` is `data class ReactionState(val liked: List<ReactionEntry>, val disliked: List<ReactionEntry>)`)
- `suspend fun write(state: ReactionState)`

### `ReactionViewModel`

New file: `ui/viewmodel/ReactionViewModel.kt`. `AndroidViewModel`.

State:
- `liked: StateFlow<List<ReactionEntry>>`
- `disliked: StateFlow<List<ReactionEntry>>`
- Convenience: `likedIds: StateFlow<Set<String>>`, `dislikedIds: StateFlow<Set<String>>` (derived).

Operations:
- `react(item: RecommendationItem, reaction: Reaction?)`:
  - `LIKED`: add to liked, remove from disliked.
  - `DISLIKED`: add to disliked, remove from liked.
  - `null`: remove from both.
  - On every change, persist to `ReactionStore`.
- Mapping `RecommendationItem -> ReactionEntry` happens at the call site by passing `descriptor`. Movie cards/details supply `genre.joinToString(", ")`; book cards/details supply `author`.

Instance is created once in `MainActivity.NextFlixApp` via `viewModel()` and passed to:
- `ResultsNavHost` → results screens
- `MovieRecommendationsScreen` / `BookRecommendationsScreen` (cards there get like/dislike too, since they reuse the same components)
- `MovieDetailScreen` / `BookDetailScreen` and `RecommendationDetailScreen`

### UI controls

- **`RecommendationResultCard`** (and the underlying `MovieResultCard` / `BookResultCard`): add a row at the bottom-right with two `IconButton`s — `ThumbUp` and `ThumbDownAlt`. The currently active reaction renders filled / tinted; the other renders outlined. Tapping the active one toggles to neutral.
- **`RecommendationDetailScreen`**: add the same two icon buttons in the existing action area (next to the back/save controls). Larger size for the detail context.
- **`MovieDetailScreen` / `BookDetailScreen`** (the legacy detail screens still used for the recommendations-flow detail): same buttons added.

The buttons are wired to `reactionViewModel.react(item, Reaction.LIKED | Reaction.DISLIKED | null)`.

### Manual filtering

In `RecommendationViewModel` (Results tab):

- Inject `dislikedIds: StateFlow<Set<String>>` (passed from `MainActivity`).
- `visibleItems` becomes a `combine` of `_contentFilter`, `_movieResults`, `_bookResults`, AND `dislikedIds`. After picking the right list, filter out any item whose id is in `dislikedIds`.

In `MovieRecommendationViewModel.generateRecommendations` (and book equivalent):

- After fetching `availableMovies` / `availableBooks` and before passing them to `recommendationService`, filter out any whose id is in the current `dislikedIds` from `ReactionStore.read()`. This handles fresh API results that the Results filter would never see.

### AI prompt changes

`RecommendationService` gains a new parameter:

```kotlin
data class ReactionContext(
    val liked: List<ReactionEntry>,
    val disliked: List<ReactionEntry>
)

suspend fun getMovieRecommendations(
    movieQuizAnswer: MovieQuizAnswer,
    personalityProfile: PersonalityQuizResult?,
    availableMovies: List<Movie>,
    reactionContext: ReactionContext       // new
): Result<List<Movie>>
```

(Same shape for `getBookRecommendations`.)

Both prompt builders append, only when at least one of liked/disliked is non-empty:

```
User's Past Reactions:
Liked: <comma-joined "Title (descriptor)">
Disliked: <comma-joined "Title (descriptor)">

Rank items more similar to the Liked entries higher, and rank items
similar to the Disliked entries lower or last. The Disliked entries
themselves have already been filtered out — do not reintroduce them.
```

Liked/Disliked sections individually omitted when their list is empty.

The VMs read `reactionStore.read()` at the start of `generateRecommendations` and pass it in.

## Affected files

**New**
- `data/recommendation/RecommendationResultsStore.kt`
- `data/reaction/ReactionModels.kt`
- `data/reaction/ReactionStore.kt`
- `ui/viewmodel/ReactionViewModel.kt`

**Modified**
- `data/personality/PersonalityQuizStore.kt` — add changed-flag key + read/write/clear.
- `ui/viewmodel/PersonalityQuizViewModel.kt` — expose `personalityChangedSinceLastRecs`; flag is set on save.
- `data/api/RecommendationService.kt` — `ReactionContext` param + prompt section, for both movies and books.
- `ui/viewmodel/MovieRecommendationViewModel.kt` — load/persist results, filter out disliked candidates, pass ReactionContext, clear changed-flag on success.
- `ui/viewmodel/BookRecommendationViewModel.kt` — same as movie VM.
- `ui/viewmodel/RecommendationViewModel.kt` — combine in `dislikedIds` to filter `visibleItems`.
- `ui/components/RecommendationResultCard.kt` (and `MovieResultCard` / `BookResultCard`) — add reaction icon buttons.
- `ui/screens/RecommendationDetailScreen.kt` — add reaction icon buttons.
- `ui/screens/MovieDetailScreen.kt` / `BookDetailScreen.kt` — add reaction icon buttons.
- `ui/screens/RecommendationResultsScreen.kt` — add dismissible banner driven by personality-changed flag.
- `MainActivity.kt` — instantiate `ReactionViewModel`, hoist personality-changed flag, add Home-tab "Retake personality quiz" button + retake routing, pass reaction state into Results/Detail/Recommendation screens.
- `ui/screens/PersonalityQuizScreen.kt` — accept optional `initialResult: PersonalityQuizResult?` for edit pre-fill.

## Edge cases

- **Cold start with no prior recs.** Stores return empty lists; Results tab shows the existing empty-state message. Behavior unchanged.
- **Disliked item is currently saved as a favorite.** Save state and dislike state are independent; favorites tab still shows it. Reaction only affects recommendation surfaces. (We can revisit if this proves confusing.)
- **All candidates filtered out by dislike filter.** Movie/book VMs surface the existing "Unable to find movies at this time" error path; user can correct by retracting a dislike on the Favorites/Detail screens.
- **Re-take personality quiz cancelled mid-way.** No save call → no flag flip → no banner. Existing behavior preserved.
- **Multiple devices / migrations.** Out of scope; DataStore is per-install.

## Testing

- Manual smoke: generate movie recs → kill app → reopen → Results tab shows them.
- Manual smoke: like a movie, dislike a movie, kill app → reopen → reactions still applied; disliked item not in Results list.
- Manual smoke: re-take personality quiz → banner appears in Results → re-run movie quiz → banner gone.
- Manual smoke: with several likes/dislikes, hit "generate" — verify (via logcat of the prompt or by inspecting outputs) that the AI prompt now contains the Liked/Disliked sections.
- No new unit tests required for this milestone; existing patterns in the repo are manual-verification first.
