package com.example.nextflix.data.saved

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nextflix.data.models.Movie
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.savedMoviesDataStore by preferencesDataStore(name = "saved_movies")

private val SavedMoviesJsonKey = stringPreferencesKey("saved_movies_json")

class SavedMoviesStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val listSerializer = ListSerializer(Movie.serializer())

    suspend fun read(): List<Movie> {
        val prefs = context.savedMoviesDataStore.data.first()
        val raw = prefs[SavedMoviesJsonKey] ?: return emptyList()
        return runCatching {
            json.decodeFromString(listSerializer, raw)
        }.getOrNull() ?: emptyList()
    }

    suspend fun write(movies: List<Movie>) {
        val encoded = json.encodeToString(listSerializer, movies)
        context.savedMoviesDataStore.edit { it[SavedMoviesJsonKey] = encoded }
    }

    suspend fun clear() {
        context.savedMoviesDataStore.edit { it.clear() }
    }
}
