package com.example.nextflix.data.quiz

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.bookQuizDataStore by preferencesDataStore(name = "book_quiz")

private val BookAnswerJsonKey = stringPreferencesKey("book_answer_json")

class BookQuizStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun read(): BookQuizAnswer? {
        val prefs = context.bookQuizDataStore.data.first()
        val raw = prefs[BookAnswerJsonKey] ?: return null
        return runCatching {
            json.decodeFromString(BookQuizAnswer.serializer(), raw)
        }.getOrNull()
    }

    suspend fun write(answer: BookQuizAnswer) {
        val encoded = json.encodeToString(BookQuizAnswer.serializer(), answer)
        context.bookQuizDataStore.edit { it[BookAnswerJsonKey] = encoded }
    }

    suspend fun clear() {
        context.bookQuizDataStore.edit { it.clear() }
    }
}
