package com.example.nextflix.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String,
    val title: String,
    val description: String = "",
    val genre: List<String> = emptyList(),
    val rating: Double? = null,
    val releaseYear: Int? = null,
    val duration: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val isSaved: Boolean = false,
    val matchScore: Double = 0.0
) {
    fun toDisplayString(): String = "$title (${releaseYear ?: "N/A"})"
    
    fun isValid(): Boolean = id.isNotBlank() && title.isNotBlank()
}

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val rating: Double? = null,
    val publishYear: Int? = null,
    val pageCount: Int? = null,
    val thumbnailUrl: String? = null,
    val language: String = "en",
    val isSaved: Boolean = false,
    val matchScore: Double = 0.0,
    val recommendationReason: String = ""
) {
    fun toDisplayString(): String = "$title by $author"
    
    fun isValid(): Boolean = id.isNotBlank() && title.isNotBlank()
}

@Serializable
data class MovieRecommendation(
    val movies: List<Movie> = emptyList(),
    val reason: String = "",
    val generatedAtEpochMillis: Long = System.currentTimeMillis()
)

@Serializable
data class BookRecommendation(
    val books: List<Book> = emptyList(),
    val reason: String = "",
    val generatedAtEpochMillis: Long = System.currentTimeMillis()
)
