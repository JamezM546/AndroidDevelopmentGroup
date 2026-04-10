package com.example.nextflix.data.quiz

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.movieQuizDataStore by preferencesDataStore(name = "movie_quiz")

private val MovieAnswerJsonKey = stringPreferencesKey("movie_answer_json")

class MovieQuizStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun read(): MovieQuizAnswer? {
        val prefs = context.movieQuizDataStore.data.first()
        val raw = prefs[MovieAnswerJsonKey] ?: return null
        return runCatching {
            json.decodeFromString(MovieQuizAnswer.serializer(), raw)
        }.getOrNull()
    }

    suspend fun write(answer: MovieQuizAnswer) {
        val encoded = json.encodeToString(MovieQuizAnswer.serializer(), answer)
        context.movieQuizDataStore.edit { it[MovieAnswerJsonKey] = encoded }
    }

    suspend fun clear() {
        context.movieQuizDataStore.edit { it.clear() }
    }
}
