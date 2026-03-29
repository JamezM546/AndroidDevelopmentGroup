package com.example.nextflix.data.personality

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.personalityQuizDataStore by preferencesDataStore(name = "personality_quiz")

private val ResultJsonKey = stringPreferencesKey("personality_result_json")

class PersonalityQuizStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun read(): PersonalityQuizResult? {
        val prefs = context.personalityQuizDataStore.data.first()
        val raw = prefs[ResultJsonKey] ?: return null
        return runCatching {
            json.decodeFromString(PersonalityQuizResult.serializer(), raw)
        }.getOrNull()
    }

    suspend fun write(result: PersonalityQuizResult) {
        val encoded = json.encodeToString(PersonalityQuizResult.serializer(), result)
        context.personalityQuizDataStore.edit { it[ResultJsonKey] = encoded }
    }
}
