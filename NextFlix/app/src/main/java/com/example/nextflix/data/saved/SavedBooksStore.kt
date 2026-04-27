package com.example.nextflix.data.saved

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nextflix.data.models.Book
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.savedBooksDataStore by preferencesDataStore(name = "saved_books")

private val SavedBooksJsonKey = stringPreferencesKey("saved_books_json")

class SavedBooksStore(private val context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val listSerializer = ListSerializer(Book.serializer())

    suspend fun read(): List<Book> {
        val prefs = context.savedBooksDataStore.data.first()
        val raw = prefs[SavedBooksJsonKey] ?: return emptyList()
        return runCatching {
            json.decodeFromString(listSerializer, raw)
        }.getOrNull() ?: emptyList()
    }

    suspend fun write(books: List<Book>) {
        val encoded = json.encodeToString(listSerializer, books)
        context.savedBooksDataStore.edit { it[SavedBooksJsonKey] = encoded }
    }

    suspend fun clear() {
        context.savedBooksDataStore.edit { it.clear() }
    }
}
