package com.example.nextflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.api.MovieApiService
import com.example.nextflix.data.api.RecommendationService
import com.example.nextflix.data.models.Movie
import com.example.nextflix.data.models.MovieRecommendation
import com.example.nextflix.data.personality.PersonalityQuizStore
import com.example.nextflix.data.quiz.MovieQuizStore
import com.example.nextflix.data.reaction.ReactionContext
import com.example.nextflix.data.reaction.ReactionEntry
import com.example.nextflix.data.reaction.ReactionStore
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationResultsStore
import com.example.nextflix.data.saved.SavedMoviesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieRecommendationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val movieApiService = MovieApiService()
    private val recommendationService = RecommendationService()
    private val movieQuizStore = MovieQuizStore(application)
    private val personalityQuizStore = PersonalityQuizStore(application)
    private val savedMoviesStore = SavedMoviesStore(application)
    private val resultsStore = RecommendationResultsStore(application)
    private val reactionStore = ReactionStore(application)

    private val _recommendations = MutableStateFlow<List<Movie>>(emptyList())
    val recommendations: StateFlow<List<Movie>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasTriggeredGeneration = false

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _generatedRecommendation = MutableStateFlow<MovieRecommendation?>(null)
    val generatedRecommendation: StateFlow<MovieRecommendation?> = _generatedRecommendation.asStateFlow()
    private val _savedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val savedMovies: StateFlow<List<Movie>> = _savedMovies.asStateFlow()

    init {
        viewModelScope.launch {
            val persisted = savedMoviesStore.read()
            if (persisted.isNotEmpty()) {
                _savedMovies.value = persisted
            }
            val savedIds = _savedMovies.value.map { it.id }.toSet()
            val persistedRecs = resultsStore.readMovies()
            if (persistedRecs.isNotEmpty()) {
                _recommendations.value = persistedRecs.map { m ->
                    m.copy(isSaved = m.id in savedIds)
                }
            }
            _savedMovies.drop(1).collect { savedMoviesStore.write(it) }
        }
    }

    fun generateRecommendations() {
        if (hasTriggeredGeneration) return
        hasTriggeredGeneration = true
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                // Load user's quiz answers
                val movieQuiz = movieQuizStore.read()
                val personalityProfile = personalityQuizStore.read()

                if (movieQuiz == null) {
                    _error.value = "Please complete the movie quiz first"
                    _isLoading.value = false
                    return@launch
                }

                // OMDB searches by title so we use known titles as proxies for genre/setting.
                // Setting terms take priority — they're often the most specific user signal.
                val genreSearchTerms = mapOf(
                    "Action" to listOf("mission impossible", "john wick", "mad max", "die hard", "speed", "taken"),
                    "Comedy" to listOf("superbad", "mean girls", "step brothers", "anchorman", "bridesmaids", "game night"),
                    "Drama" to listOf("shawshank", "forrest gump", "green mile", "schindler", "beautiful mind", "pursuit of happyness"),
                    "Sci-Fi" to listOf("interstellar", "inception", "blade runner", "the matrix", "arrival", "ex machina"),
                    "Horror" to listOf("conjuring", "hereditary", "get out", "scream", "midsommar", "sinister"),
                    "Romance" to listOf("pride prejudice", "when harry met sally", "la la land", "before sunrise", "crazy rich", "notebook")
                )
                val settingSearchTerms = mapOf(
                    "Fantasy world" to listOf("lord of the rings", "harry potter", "the hobbit", "narnia", "princess bride", "willow", "labyrinth", "eragon", "dragonheart"),
                    "Future/Space" to listOf("star wars", "guardians of the galaxy", "dune", "fifth element", "total recall", "minority report", "edge of tomorrow", "oblivion"),
                    "Historical period" to listOf("gladiator", "braveheart", "300", "troy", "last samurai", "kingdom of heaven", "master and commander"),
                    "Real world" to emptyList(),
                    "Anywhere interesting" to emptyList()
                )

                val genreTerms = genreSearchTerms[movieQuiz.genre] ?: emptyList()
                val settingTerms = settingSearchTerms[movieQuiz.setting] ?: emptyList()
                // Setting terms first so the candidate pool skews toward the world the user wants
                val searchTerms = (settingTerms + genreTerms).distinct().ifEmpty { listOf("popular movies") }

                val allMovies = mutableListOf<Movie>()
                for (term in searchTerms) {
                    val results = movieApiService.searchMovies(term, maxResults = 2).getOrNull() ?: emptyList()
                    allMovies.addAll(results)
                }
                // Deduplicate by ID; take more candidates so the AI has a richer pool to rank
                var availableMovies = allMovies.distinctBy { it.id }.take(15)

                // Fallback if nothing came back
                if (availableMovies.isEmpty()) {
                    Log.d("MovieRecommendationVM", "No results from genre searches, trying generic")
                    availableMovies = movieApiService.searchMovies("popular", maxResults = 10).getOrNull() ?: emptyList()
                }

                // If still no results, use some popular movies as fallback
                if (availableMovies.isEmpty()) {
                    Log.d("MovieRecommendationVM", "No results from API, using fallback popular movies")
                    availableMovies = getFallbackMovies()
                }

                // Filter out any previously disliked items before sending to AI
                val reactions = reactionStore.read()
                val dislikedIds = reactions.disliked.map { it.id }.toSet()
                availableMovies = availableMovies.filterNot { it.id in dislikedIds }

                // Fetch full details (description, rating, genre) for each movie
                availableMovies = availableMovies.mapNotNull { movie ->
                    try {
                        movieApiService.getMovieDetails(movie.id).getOrNull()
                    } catch (e: Exception) {
                        Log.w("MovieRecommendationVM", "Failed to get details for ${movie.title}", e)
                        movie
                    }
                }

                if (availableMovies.isEmpty()) {
                    _error.value = "Unable to find movies at this time. Please try again later."
                    _isLoading.value = false
                    return@launch
                }

                // Build reaction context for AI prompt
                val reactionContext = ReactionContext(
                    liked = reactions.liked.filter { it.contentType == RecommendationContentType.MOVIE },
                    disliked = reactions.disliked.filter { it.contentType == RecommendationContentType.MOVIE }
                )

                // Get AI-powered recommendations
                val recommendationResult = recommendationService.getMovieRecommendations(
                    movieQuiz,
                    personalityProfile,
                    availableMovies,
                    reactionContext
                )

                val rankedMovies = recommendationResult.getOrNull() ?: availableMovies
                val savedIds = _savedMovies.value.map { it.id }.toSet()
                _recommendations.value = rankedMovies.map { movie ->
                    movie.copy(isSaved = movie.id in savedIds)
                }
                _generatedRecommendation.value = MovieRecommendation(
                    movies = rankedMovies,
                    reason = if (movieQuiz.era.isBlank()) {
                        "Based on your preferences for ${movieQuiz.genre}"
                    } else {
                        "Based on your preferences for ${movieQuiz.genre} from the ${movieQuiz.era} era"
                    }
                )

                // Persist results and clear personality-changed flag
                resultsStore.writeMovies(rankedMovies)
                personalityQuizStore.clearChangedFlag()

                _isLoading.value = false

                Log.d("MovieRecommendationVM", "Generated ${rankedMovies.size} recommendations")
            } catch (e: Exception) {
                _error.value = "Error generating recommendations: ${e.message}"
                _isLoading.value = false
                Log.e("MovieRecommendationVM", "Error", e)
            }
        }
    }
    
    private fun getFallbackMovies(): List<Movie> {
        // Return some popular movies as fallback when API fails
        return listOf(
            Movie(
                id = "tt0111161",
                title = "The Shawshank Redemption",
                releaseYear = 1994,
                rating = 9.3,
                description = "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                genre = listOf("Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMDFlYTk5YzktYTBkYi00ZmUxLWFmZTUtOTNjNDI2ZTBjNDU3XkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0068646",
                title = "The Godfather",
                releaseYear = 1972,
                rating = 9.2,
                description = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his youngest and reluctant son.",
                genre = listOf("Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNmUtN2Y1OTM0MzVlM2Y0XkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0071562",
                title = "The Godfather Part II",
                releaseYear = 1974,
                rating = 9.0,
                description = "The early life and family tree of Vito Corleone is portrayed from his start as an orphate in Sicily to his rise in New York during the Prohibition era.",
                genre = listOf("Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BNzcyODk0NjEtNWEwOC00ZDZkLWJhNTgtOWZhYjE1ZWM0MWFmXkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0468569",
                title = "The Dark Knight",
                releaseYear = 2008,
                rating = 9.0,
                description = "When the menace known as The Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.",
                genre = listOf("Action", "Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0050083",
                title = "12 Angry Men",
                releaseYear = 1957,
                rating = 8.9,
                description = "A jury holdout attempts to prevent a miscarriage of justice by forcing his colleagues to reconsider the evidence.",
                genre = listOf("Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMWU4N2FjNzYtNTVhNC00NjZlLWI5ZTAtODkzN2FhODg3MzBiXkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0110912",
                title = "Pulp Fiction",
                releaseYear = 1994,
                rating = 8.9,
                description = "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.",
                genre = listOf("Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItMDJlODkzNDIxMDA1XkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0816692",
                title = "Interstellar",
                releaseYear = 2014,
                rating = 8.6,
                description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                genre = listOf("Adventure", "Drama", "Sci-Fi"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDAtMDEwOC00ZDZkLWJkZjItZWU5MWMxMmQyNzA1XkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0109830",
                title = "Forrest Gump",
                releaseYear = 1994,
                rating = 8.8,
                description = "The presidencies of Kennedy and Johnson, the Vietnam War, and the Watergate scandal unfold from the perspective of an Alabama man with an IQ of 75.",
                genre = listOf("Drama", "Romance"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BNWIwODRlZTUtY2U3ZS00Ym9hLWY0ZTUtOTdhMDVmNzcyZjExXkEyXkFqcGc@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt1345836",
                title = "The Dark Knight Rises",
                releaseYear = 2012,
                rating = 8.4,
                description = "Eight years after the Joker's reign of anarchy, Batman and hero Harvey Dent successfully rid Gotham of organized crime.",
                genre = listOf("Action", "Crime", "Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMTk4ODQzNDY3Nl5BMl5BanBnXkFtZTcwODA0NTM4Nw@@._V1_SX300.jpg"
            ),
            Movie(
                id = "tt0137523",
                title = "Fight Club",
                releaseYear = 1999,
                rating = 8.8,
                description = "An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into much more.",
                genre = listOf("Drama"),
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMmEzNTA0ZDctZTVjOS00ZGUwLWE4MmQtZjg0YjVjOWEwOWY0XkEyXkFqcGc@._V1_SX300.jpg"
            )
        )
    }

    fun resetForNewGeneration() {
        hasTriggeredGeneration = false
    }

    fun saveMovie(movie: Movie) {
        _savedMovies.update { current ->
            if (current.any { it.id == movie.id }) current
            else current + movie.copy(isSaved = true)
        }
        _recommendations.update { movies ->
            movies.map { m ->
                if (m.id == movie.id) m.copy(isSaved = true) else m
            }
        }
    }

    fun unsaveMovie(movieId: String) {
        _savedMovies.update { current -> current.filterNot { it.id == movieId } }
        _recommendations.update { movies ->
            movies.map { m ->
                if (m.id == movieId) m.copy(isSaved = false) else m
            }
        }
    }

    fun isMovieSaved(movieId: String): Boolean =
        _savedMovies.value.any { it.id == movieId }
}
