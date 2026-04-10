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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieRecommendationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val movieApiService = MovieApiService()
    private val recommendationService = RecommendationService()
    private val movieQuizStore = MovieQuizStore(application)
    private val personalityQuizStore = PersonalityQuizStore(application)

    private val _recommendations = MutableStateFlow<List<Movie>>(emptyList())
    val recommendations: StateFlow<List<Movie>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _generatedRecommendation = MutableStateFlow<MovieRecommendation?>(null)
    val generatedRecommendation: StateFlow<MovieRecommendation?> = _generatedRecommendation.asStateFlow()

    fun generateRecommendations() {
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

                // Build search query
                var searchQuery = "${movieQuiz.genre} ${movieQuiz.era}"
                var availableMovies = movieApiService.searchMovies(searchQuery, maxResults = 20).getOrNull() ?: emptyList()
                
                // If no results with combined query, try just genre
                if (availableMovies.isEmpty()) {
                    Log.d("MovieRecommendationVM", "No results for '$searchQuery', trying genre only")
                    availableMovies = movieApiService.searchMovies(movieQuiz.genre, maxResults = 20).getOrNull() ?: emptyList()
                }
                
                // If still no results, try era
                if (availableMovies.isEmpty()) {
                    Log.d("MovieRecommendationVM", "No results for genre, trying era")
                    availableMovies = movieApiService.searchMovies(movieQuiz.era, maxResults = 20).getOrNull() ?: emptyList()
                }
                
                // If still no results, use some popular movies as fallback
                if (availableMovies.isEmpty()) {
                    Log.d("MovieRecommendationVM", "No results from API, using fallback popular movies")
                    availableMovies = getFallbackMovies()
                }

                if (availableMovies.isEmpty()) {
                    _error.value = "Unable to find movies at this time. Please try again later."
                    _isLoading.value = false
                    return@launch
                }

                // Get AI-powered recommendations
                val recommendationResult = recommendationService.getMovieRecommendations(
                    movieQuiz,
                    personalityProfile,
                    availableMovies
                )

                val rankedMovies = recommendationResult.getOrNull() ?: availableMovies
                _recommendations.value = rankedMovies
                _generatedRecommendation.value = MovieRecommendation(
                    movies = rankedMovies,
                    reason = "Based on your preferences for ${movieQuiz.genre} from the ${movieQuiz.era} era"
                )
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
}
