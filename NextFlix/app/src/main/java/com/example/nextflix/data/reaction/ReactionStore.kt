package com.example.nextflix.data.reaction

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.reactionsDataStore by preferencesDataStore(name = "reactions")

private val LikedJsonKey = stringPreferencesKey("liked_json")
private val DislikedJsonKey = stringPreferencesKey("disliked_json")

class ReactionStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val listSerializer = ListSerializer(ReactionEntry.serializer())

    suspend fun read(): ReactionState {
        val prefs = context.reactionsDataStore.data.first()
        val liked = prefs[LikedJsonKey]?.let {
            runCatching { json.decodeFromString(listSerializer, it) }.getOrNull()
        } ?: emptyList()
        val disliked = prefs[DislikedJsonKey]?.let {
            runCatching { json.decodeFromString(listSerializer, it) }.getOrNull()
        } ?: emptyList()
        return ReactionState(liked = liked, disliked = disliked)
    }

    suspend fun write(state: ReactionState) {
        val likedEncoded = json.encodeToString(listSerializer, state.liked)
        val dislikedEncoded = json.encodeToString(listSerializer, state.disliked)
        context.reactionsDataStore.edit {
            it[LikedJsonKey] = likedEncoded
            it[DislikedJsonKey] = dislikedEncoded
        }
    }
}
