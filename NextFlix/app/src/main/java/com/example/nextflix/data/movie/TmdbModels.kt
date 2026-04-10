package com.example.nextflix.data.movie

import com.google.gson.annotations.SerializedName

class TmdbMovie {
    @JvmField
    @SerializedName("id")
    var id: Int? = null

    @JvmField
    @SerializedName("title")
    var title: String? = null

    @JvmField
    @SerializedName("overview")
    var overview: String? = null

    @JvmField
    @SerializedName("poster_path")
    var posterPath: String? = null

    @JvmField
    @SerializedName("release_date")
    var releaseDate: String? = null

    @JvmField
    @SerializedName("vote_average")
    var voteAverage: Double? = null

    @JvmField
    @SerializedName("genre_ids")
    var genreIds: List<Int>? = null

    // Convenience property to get the full poster URL
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

class TmdbResponse {
    @SerializedName("results")
    var results: List<TmdbMovie>? = null

    @SerializedName("total_results")
    var totalResults: Int? = null
}
