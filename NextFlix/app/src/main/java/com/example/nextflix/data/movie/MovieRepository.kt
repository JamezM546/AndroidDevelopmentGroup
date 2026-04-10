package com.example.nextflix.data.movie

import android.util.Log
import com.example.nextflix.BuildConfig
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieRepository {

    private val apiService: TmdbApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(TmdbApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(TmdbApiService::class.java)
    }

    // Maps quiz genre answer to TMDB genre ID
    private fun mapGenre(genre: String): String? {
        return when (genre) {
            "Action" -> "28"
            "Comedy" -> "35"
            "Drama" -> "18"
            "Sci-Fi" -> "878"
            "Horror" -> "27"
            "Romance" -> "10749"
            else -> null
        }
    }

    // Maps quiz era answer to a date range
    private fun mapEra(era: String): Pair<String?, String?> {
        return when (era) {
            "Classic (before 1980)" -> Pair(null, "1979-12-31")
            "Golden Age (1980-2000)" -> Pair("1980-01-01", "2000-12-31")
            "Modern (2000-2015)" -> Pair("2000-01-01", "2015-12-31")
            "Recent (2015+)" -> Pair("2015-01-01", null)
            else -> Pair(null, null)
        }
    }

    // Converts a TmdbMovie into James's RecommendationItem format
    fun toRecommendationItem(movie: TmdbMovie): RecommendationItem {
        return RecommendationItem(
            id = "tmdb_${movie.id}",
            contentType = RecommendationContentType.MOVIE,
            title = movie.title ?: "Unknown Title",
            imageUrl = movie.posterUrl,
            summary = movie.overview ?: "No description available.",
            rating = movie.voteAverage?.let { "%.1f/10".format(it) }
        )
    }

    /**
     * Fetch movies based on quiz answers.
     * quizAnswers is a map of question ID to selected answer string.
     */
    fun fetchMovies(
        quizAnswers: Map<Int, String>,
        onSuccess: (List<TmdbMovie>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Question 1 = genre, Question 3 = era
        val genreId = quizAnswers[1]?.let { mapGenre(it) }
        val (dateGte, dateLte) = quizAnswers[3]?.let { mapEra(it) } ?: Pair(null, null)

        val call = apiService.discoverMovies(
            apiKey = BuildConfig.TMDB_API_KEY,
            genres = genreId,
            releaseDateGte = dateGte,
            releaseDateLte = dateLte
        )

        call.enqueue(object : Callback<TmdbResponse> {
            override fun onResponse(call: Call<TmdbResponse>, response: Response<TmdbResponse>) {
                if (response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    Log.d("MovieRepository", "Fetched ${movies.size} movies")
                    onSuccess(movies)
                } else {
                    Log.e("MovieRepository", "API error: ${response.code()}")
                    onFailure("API error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TmdbResponse>, t: Throwable) {
                Log.e("MovieRepository", "Network error: ${t.message}")
                onFailure("Network error: ${t.message}")
            }
        })
    }
}
