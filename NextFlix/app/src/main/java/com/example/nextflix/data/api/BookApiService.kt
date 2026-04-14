package com.example.nextflix.data.api

import android.util.Log
import com.example.nextflix.BuildConfig
import com.example.nextflix.data.models.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class BookApiService {
    
    suspend fun searchBooks(query: String, maxResults: Int = 10): Result<List<Book>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=$maxResults&key=${BuildConfig.GOOGLE_BOOKS_API_KEY}"
            
            val response = fetchUrl(urlString)
            val books = parseGoogleBooksResponse(response)
            Result.success(books)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search books", e)
            Result.failure(e)
        }
    }
    
    suspend fun searchBooksByGenre(genre: String, maxResults: Int = 10): Result<List<Book>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val query = "$genre fiction"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=$maxResults&key=${BuildConfig.GOOGLE_BOOKS_API_KEY}"
            
            val response = fetchUrl(urlString)
            val books = parseGoogleBooksResponse(response)
            Result.success(books)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search books by genre", e)
            Result.failure(e)
        }
    }
    
    private fun fetchUrl(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        return BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.readText()
        }
    }
    
    private fun parseGoogleBooksResponse(jsonString: String): List<Book> {
        val books = mutableListOf<Book>()
        
        try {
            val root = JSONObject(jsonString)
            val items = root.optJSONArray("items") ?: return books
            
            for (i in 0 until items.length()) {
                try {
                    val item = items.getJSONObject(i)
                    val volumeInfo = item.getJSONObject("volumeInfo")
                    
                    val id = item.getString("id")
                    val title = volumeInfo.optString("title", "Unknown")
                    val authors = volumeInfo.optJSONArray("authors")
                    val author = if (authors != null && authors.length() > 0) {
                        authors.getString(0)
                    } else {
                        "Unknown Author"
                    }
                    
                    val description = volumeInfo.optString("description", "")
                    val categories = volumeInfo.optJSONArray("categories")
                    val genres = mutableListOf<String>()
                    if (categories != null) {
                        for (j in 0 until categories.length()) {
                            genres.add(categories.getString(j))
                        }
                    }
                    
                    val rating = volumeInfo.optDouble("averageRating", 0.0)
                    val publishedDate = volumeInfo.optString("publishedDate", "")
                    val publishYear = try {
                        publishedDate.take(4).toIntOrNull()
                    } catch (e: Exception) {
                        null
                    }
                    
                    val pageCount = volumeInfo.optInt("pageCount", 0)
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val thumbnailUrl = imageLinks?.optString("thumbnail", null)
                        ?.replace("http://", "https://")
                    
                    val book = Book(
                        id = id,
                        title = title,
                        author = author,
                        description = description,
                        genres = genres,
                        rating = if (rating > 0) rating else null,
                        publishYear = publishYear,
                        pageCount = if (pageCount > 0) pageCount else null,
                        thumbnailUrl = thumbnailUrl,
                        language = volumeInfo.optString("language", "en")
                    )
                    
                    books.add(book)
                } catch (e: JSONException) {
                    Log.w(TAG, "Failed to parse book item", e)
                    continue
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse Google Books response", e)
        }
        
        return books
    }
    
    companion object {
        private const val TAG = "BookApiService"
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> failure(exception: Exception): Result<T> = Error(exception)
    }
}
