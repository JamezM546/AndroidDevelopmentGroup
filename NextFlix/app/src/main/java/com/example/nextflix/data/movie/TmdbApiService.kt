package com.example.nextflix.data.movie

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    // Discover movies filtered by genre, year range, etc.
    @GET("discover/movie")
    fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genres: String? = null,
        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): Call<TmdbResponse>

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }
}
