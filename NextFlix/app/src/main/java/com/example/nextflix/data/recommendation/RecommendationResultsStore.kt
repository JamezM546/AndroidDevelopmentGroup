package com.example.nextflix.data.recommendation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nextflix.data.models.Book
import com.example.nextflix.data.models.Movie
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.recommendationResultsDataStore by preferencesDataStore(name = "recommendation_results")

private val MoviesJsonKey = stringPreferencesKey("movies_json")
private val BooksJsonKey = stringPreferencesKey("books_json")

class RecommendationResultsStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val movieListSerializer = ListSerializer(Movie.serializer())
    private val bookListSerializer = ListSerializer(Book.serializer())

    suspend fun readMovies(): List<Movie> {
        val raw = context.recommendationResultsDataStore.data.first()[MoviesJsonKey] ?: return emptyList()
        return runCatching { json.decodeFromString(movieListSerializer, raw) }.getOrNull() ?: emptyList()
    }

    suspend fun writeMovies(movies: List<Movie>) {
        val encoded = json.encodeToString(movieListSerializer, movies)
        context.recommendationResultsDataStore.edit { it[MoviesJsonKey] = encoded }
    }

    suspend fun readBooks(): List<Book> {
        val raw = context.recommendationResultsDataStore.data.first()[BooksJsonKey] ?: return emptyList()
        return runCatching { json.decodeFromString(bookListSerializer, raw) }.getOrNull() ?: emptyList()
    }

    suspend fun writeBooks(books: List<Book>) {
        val encoded = json.encodeToString(bookListSerializer, books)
        context.recommendationResultsDataStore.edit { it[BooksJsonKey] = encoded }
    }
}
