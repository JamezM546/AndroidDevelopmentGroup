package com.example.nextflix.data.api

import android.util.Log
import com.example.nextflix.data.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MovieApiService {
    
    suspend fun searchMovies(query: String, maxResults: Int = 10): Result<List<Movie>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Try primary search
            val movies = performSearch(query, maxResults)
            
            // If no results, try searching just the first keyword
            if (movies.isEmpty() && query.contains(" ")) {
                Log.d(TAG, "No results for '$query', trying first keyword")
                val firstKeyword = query.split(" ")[0]
                return@withContext Result.success(performSearch(firstKeyword, maxResults))
            }
            
            // If still no results, try some common search terms
            if (movies.isEmpty()) {
                Log.d(TAG, "No results for '$query', trying popular movies")
                val popularMovies = performSearch("popular", maxResults)
                if (popularMovies.isNotEmpty()) {
                    return@withContext Result.success(popularMovies)
                }
            }
            
            Result.success(movies)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search movies", e)
            Result.failure(e)
        }
    }
    
    private suspend fun performSearch(query: String, maxResults: Int): List<Movie> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlString = "https://www.omdbapi.com/?s=$encodedQuery&type=movie&apikey=$OMDB_API_KEY"
        
        Log.d(TAG, "Searching OMDB for: $query")
        
        val response = fetchUrl(urlString)
        parseOMDBResponse(response).take(maxResults)
    }
    
    suspend fun getMovieDetails(imdbId: String): Result<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val urlString = "https://www.omdbapi.com/?i=$imdbId&apikey=$OMDB_API_KEY"
            
            val response = fetchUrl(urlString)
            val movie = parseOMDBMovieDetails(response)
            Result.success(movie)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get movie details", e)
            Result.failure(e)
        }
    }
    
    suspend fun searchMoviesByGenre(genre: String, maxResults: Int = 10): Result<List<Movie>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val encodedGenre = URLEncoder.encode(genre, "UTF-8")
            val urlString = "https://www.omdbapi.com/?s=$encodedGenre&type=movie&apikey=$OMDB_API_KEY"
            
            val response = fetchUrl(urlString)
            val movies = parseOMDBResponse(response)
            Result.success(movies.take(maxResults))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search movies by genre", e)
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
    
    private fun parseOMDBResponse(jsonString: String): List<Movie> {
        val movies = mutableListOf<Movie>()
        
        try {
            val root = JSONObject(jsonString)
            val response = root.optString("Response", "False")
            
            if (response != "True") {
                Log.w(TAG, "OMDB Error: ${root.optString("Error", "Unknown error")}")
                return movies
            }
            
            val searchResults = root.optJSONArray("Search") ?: return movies
            
            for (i in 0 until searchResults.length()) {
                try {
                    val item = searchResults.getJSONObject(i)
                    
                    val movie = Movie(
                        id = item.getString("imdbID"),
                        title = item.getString("Title"),
                        releaseYear = try {
                            item.optString("Year", "N/A").toIntOrNull()
                        } catch (e: Exception) {
                            null
                        },
                        posterUrl = item.optString("Poster", "N/A").takeIf { it != "N/A" },
                        description = "",
                        genre = emptyList()
                    )
                    
                    movies.add(movie)
                    Log.d(TAG, "Found movie: ${movie.title}")
                } catch (e: JSONException) {
                    Log.w(TAG, "Failed to parse movie item", e)
                    continue
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse OMDB response", e)
        }
        
        Log.d(TAG, "Parsed ${movies.size} movies")
        return movies
    }
    
    private fun parseOMDBMovieDetails(jsonString: String): Movie {
        val root = JSONObject(jsonString)
        
        val response = root.optString("Response", "False")
        if (response != "True") {
            throw Exception("OMDB Error: ${root.optString("Error", "Unknown error")}")
        }
        
        val imdbId = root.getString("imdbID")
        val title = root.getString("Title")
        val description = root.optString("Plot", "")
        val genreString = root.optString("Genre", "")
        val genres = if (genreString.isNotEmpty()) {
            genreString.split(",").map { it.trim() }
        } else {
            emptyList()
        }
        
        val rating = try {
            val ratingString = root.optString("imdbRating", "N/A")
            if (ratingString != "N/A") ratingString.toDoubleOrNull() else null
        } catch (e: Exception) {
            null
        }
        
        val releaseYear = try {
            root.optString("Year", "N/A").toIntOrNull()
        } catch (e: Exception) {
            null
        }
        
        val duration = try {
            val runtimeString = root.optString("Runtime", "N/A")
            if (runtimeString != "N/A") {
                runtimeString.replace(Regex("[^0-9]"), "").toIntOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        val posterUrl = root.optString("Poster", "N/A").takeIf { it != "N/A" }
        
        return Movie(
            id = imdbId,
            title = title,
            description = description,
            genre = genres,
            rating = rating,
            releaseYear = releaseYear,
            duration = duration,
            posterUrl = posterUrl
        )
    }
    
    companion object {
        private const val TAG = "MovieApiService"
        private const val OMDB_API_KEY = "7740abfc"
    }
}
