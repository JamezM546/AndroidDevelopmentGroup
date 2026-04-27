package com.example.nextflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.api.BookApiService
import com.example.nextflix.data.api.RecommendationService
import com.example.nextflix.data.models.Book
import com.example.nextflix.data.models.BookRecommendation
import com.example.nextflix.data.personality.PersonalityQuizStore
import com.example.nextflix.data.quiz.BookQuizStore
import com.example.nextflix.data.reaction.ReactionContext
import com.example.nextflix.data.reaction.ReactionStore
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationResultsStore
import com.example.nextflix.data.saved.SavedBooksStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookRecommendationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val bookApiService = BookApiService()
    private val recommendationService = RecommendationService()
    private val bookQuizStore = BookQuizStore(application)
    private val personalityQuizStore = PersonalityQuizStore(application)
    private val savedBooksStore = SavedBooksStore(application)
    private val resultsStore = RecommendationResultsStore(application)
    private val reactionStore = ReactionStore(application)

    private val _recommendations = MutableStateFlow<List<Book>>(emptyList())
    val recommendations: StateFlow<List<Book>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasTriggeredGeneration = false

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _generatedRecommendation = MutableStateFlow<BookRecommendation?>(null)
    val generatedRecommendation: StateFlow<BookRecommendation?> = _generatedRecommendation.asStateFlow()
    private val _savedBooks = MutableStateFlow<List<Book>>(emptyList())
    val savedBooks: StateFlow<List<Book>> = _savedBooks.asStateFlow()

    init {
        viewModelScope.launch {
            val persisted = savedBooksStore.read()
            if (persisted.isNotEmpty()) {
                _savedBooks.value = persisted
            }
            val savedIds = _savedBooks.value.map { it.id }.toSet()
            val persistedRecs = resultsStore.readBooks()
            if (persistedRecs.isNotEmpty()) {
                _recommendations.value = persistedRecs.map { b ->
                    b.copy(isSaved = b.id in savedIds)
                }
            }
            _savedBooks.drop(1).collect { savedBooksStore.write(it) }
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
                val bookQuiz = bookQuizStore.read()
                val personalityProfile = personalityQuizStore.read()

                if (bookQuiz == null) {
                    _error.value = "Please complete the book quiz first"
                    _isLoading.value = false
                    return@launch
                }

                // Map setting values to concrete search terms that surface relevant books.
                // Google Books search works best with specific genre/world keywords.
                val settingKeywords = mapOf(
                    "Fantasy world" to "fantasy magic world",
                    "Space / futuristic" to "science fiction space future",
                    "Historical" to "historical fiction",
                    "Modern day" to ""
                )
                val settingTerm = settingKeywords[bookQuiz.setting] ?: bookQuiz.setting
                val searchQuery = listOf(bookQuiz.genre, settingTerm, bookQuiz.mood)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "fiction books" }
                val booksResult = bookApiService.searchBooks(searchQuery, maxResults = 20)
                val availableBooks = booksResult.getOrNull() ?: emptyList()

                if (availableBooks.isEmpty()) {
                    _error.value = "No books found. Please try different preferences."
                    _isLoading.value = false
                    return@launch
                }

                // Filter out previously disliked items before sending to AI
                val reactions = reactionStore.read()
                val dislikedIds = reactions.disliked.map { it.id }.toSet()
                val filteredBooks = availableBooks.filterNot { it.id in dislikedIds }

                // Build reaction context for AI prompt
                val reactionContext = ReactionContext(
                    liked = reactions.liked.filter { it.contentType == RecommendationContentType.BOOK },
                    disliked = reactions.disliked.filter { it.contentType == RecommendationContentType.BOOK }
                )

                // Get AI-powered recommendations
                val recommendationResult = recommendationService.getBookRecommendations(
                    bookQuiz,
                    personalityProfile,
                    filteredBooks,
                    reactionContext
                )

                val rankedBooks = recommendationResult.getOrNull() ?: filteredBooks
                val savedIds = _savedBooks.value.map { it.id }.toSet()
                _recommendations.value = rankedBooks.map { book ->
                    book.copy(isSaved = book.id in savedIds)
                }
                _generatedRecommendation.value = BookRecommendation(
                    books = rankedBooks,
                    reason = if (bookQuiz.mood.isBlank()) {
                        "Based on your preferences for ${bookQuiz.genre}"
                    } else {
                        "Based on your preferences for ${bookQuiz.genre} with a ${bookQuiz.mood} mood"
                    }
                )

                // Persist results and clear personality-changed flag
                resultsStore.writeBooks(rankedBooks)
                personalityQuizStore.clearChangedFlag()

                Log.d(TAG, "Generated ${rankedBooks.size} book recommendations")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate recommendations", e)
                _error.value = "Failed to generate recommendations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetForNewGeneration() {
        hasTriggeredGeneration = false
    }

    fun saveBook(book: Book) {
        _savedBooks.update { current ->
            if (current.any { it.id == book.id }) current
            else current + book.copy(isSaved = true)
        }
        _recommendations.update { books ->
            books.map { b ->
                if (b.id == book.id) b.copy(isSaved = true) else b
            }
        }
    }

    fun unsaveBook(bookId: String) {
        _savedBooks.update { current -> current.filterNot { it.id == bookId } }
        _recommendations.update { books ->
            books.map { b ->
                if (b.id == bookId) b.copy(isSaved = false) else b
            }
        }
    }

    fun isBookSaved(bookId: String): Boolean =
        _savedBooks.value.any { it.id == bookId }

    companion object {
        private const val TAG = "BookRecommendationVM"
    }
}
