package com.example.nextflix.data.api

import android.util.Log
import com.example.nextflix.data.models.Book
import com.example.nextflix.data.models.Movie
import com.example.nextflix.data.quiz.BookQuizAnswer
import com.example.nextflix.data.quiz.MovieQuizAnswer
import com.example.nextflix.data.personality.PersonalityQuizResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RecommendationService {
    
    suspend fun getBookRecommendations(
        bookQuizAnswer: BookQuizAnswer,
        personalityProfile: PersonalityQuizResult?,
        availableBooks: List<Book>
    ): Result<List<Book>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Build a prompt for the AI
            val prompt = buildBookRecommendationPrompt(bookQuizAnswer, personalityProfile, availableBooks)
            
            // Call Gemini API
            val ranking = callGeminiAPI(prompt, availableBooks.map { it.id })
            
            // Apply ranking to books
            val rankedBooks = applyBookRanking(availableBooks, ranking)
            
            Result.success(rankedBooks)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get book recommendations", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMovieRecommendations(
        movieQuizAnswer: MovieQuizAnswer,
        personalityProfile: PersonalityQuizResult?,
        availableMovies: List<Movie>
    ): Result<List<Movie>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Build a prompt for the AI
            val prompt = buildMovieRecommendationPrompt(movieQuizAnswer, personalityProfile, availableMovies)
            
            // Call Gemini API
            val ranking = callGeminiAPI(prompt, availableMovies.map { it.id })
            
            // Apply ranking to movies
            val rankedMovies = applyMovieRanking(availableMovies, ranking)
            
            Result.success(rankedMovies)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get movie recommendations", e)
            Result.failure(e)
        }
    }
    
    private fun buildBookRecommendationPrompt(
        bookQuiz: BookQuizAnswer,
        personalityProfile: PersonalityQuizResult?,
        availableBooks: List<Book>
    ): String {
        return """
            You are a book recommendation expert. Based on the user's preferences, rank these books from best to least suitable.
            
            User's Book Preferences:
            - Genre: ${bookQuiz.genre}
            - Mood: ${bookQuiz.mood}
            - Length: ${bookQuiz.length}
            - Pace: ${bookQuiz.pace}
            - Setting: ${bookQuiz.setting}
            - Audience: ${bookQuiz.audience}
            
            ${if (personalityProfile != null) {
                "User's Personality Profile: ${personalityProfile.answers.entries.joinToString(", ") { "${it.key}: ${it.value}" }}"
            } else {
                ""
            }}
            
            Available Books (ID: Title - Author):
            ${availableBooks.joinToString("\n") { "${it.id}: ${it.title} by ${it.author}" }}
            
            Return ONLY a JSON array of book IDs in order of recommendation, like: ["id1", "id2", "id3"]
            Do not include any other text.
        """.trimIndent()
    }
    
    private fun buildMovieRecommendationPrompt(
        movieQuiz: MovieQuizAnswer,
        personalityProfile: PersonalityQuizResult?,
        availableMovies: List<Movie>
    ): String {
        return """
            You are a movie recommendation expert. Based on the user's preferences, rank these movies from best to least suitable.
            
            User's Movie Preferences:
            - Genre: ${movieQuiz.genre}
            - Duration: ${movieQuiz.duration}
            - Era: ${movieQuiz.era}
            - Ending: ${movieQuiz.ending}
            - Special Effects: ${movieQuiz.specialEffects}
            - Setting: ${movieQuiz.setting}
            
            ${if (personalityProfile != null) {
                "User's Personality Profile: ${personalityProfile.answers.entries.joinToString(", ") { "${it.key}: ${it.value}" }}"
            } else {
                ""
            }}
            
            Available Movies (ID: Title - Year):
            ${availableMovies.joinToString("\n") { "${it.id}: ${it.title} (${it.releaseYear ?: "N/A"})" }}
            
            Return ONLY a JSON array of movie IDs in order of recommendation, like: ["id1", "id2", "id3"]
            Do not include any other text.
        """.trimIndent()
    }
    
    private fun callGeminiAPI(prompt: String, expectedIds: List<String>): List<String> {
        return try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GEMINI_API_KEY"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Content-Type", "application/json")
            connection.requestMethod = "POST"
            connection.doOutput = true
            
            val requestBody = JSONObject()
            val contentsArray = JSONArray()
            val contentsObj = JSONObject()
            val partsArray = JSONArray()
            val partsObj = JSONObject()
            partsObj.put("text", prompt)
            partsArray.put(partsObj)
            contentsObj.put("parts", partsArray)
            contentsArray.put(contentsObj)
            requestBody.put("contents", contentsArray)
            
            // Write request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            // Read response
            val responseText = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
            
            val responseJson = JSONObject(responseText)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val textPart = parts.getJSONObject(0)
                    val text = textPart.getString("text").trim()
                    
                    // Parse the JSON array from response
                    // Gemini might wrap JSON in markdown code blocks
                    val jsonText = if (text.startsWith("```")) {
                        text.substringAfter("```json").substringAfter("```").substringBeforeLast("```").trim()
                    } else {
                        text
                    }

                    val jsonArray = JSONArray(jsonText)
                    val ranking = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        ranking.add(jsonArray.getString(i))
                    }
                    return ranking
                }
            }
            
            expectedIds
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API", e)
            expectedIds
        }
    }
    
    private fun applyBookRanking(books: List<Book>, ranking: List<String>): List<Book> {
        val bookMap = books.associateBy { it.id }
        val rankedBooks = mutableListOf<Book>()
        
        // Add ranked books first
        ranking.forEach { id ->
            bookMap[id]?.let { 
                rankedBooks.add(it.copy(matchScore = (1.0 - (rankedBooks.size * 0.1)).coerceIn(0.0, 1.0)))
            }
        }
        
        // Add any remaining books
        books.forEach { book ->
            if (!rankedBooks.any { it.id == book.id }) {
                rankedBooks.add(book.copy(matchScore = 0.2))
            }
        }
        
        return rankedBooks
    }
    
    private fun applyMovieRanking(movies: List<Movie>, ranking: List<String>): List<Movie> {
        val movieMap = movies.associateBy { it.id }
        val rankedMovies = mutableListOf<Movie>()
        
        // Add ranked movies first
        ranking.forEach { id ->
            movieMap[id]?.let { 
                rankedMovies.add(it.copy(matchScore = (1.0 - (rankedMovies.size * 0.1)).coerceIn(0.0, 1.0)))
            }
        }
        
        // Add any remaining movies
        movies.forEach { movie ->
            if (!rankedMovies.any { it.id == movie.id }) {
                rankedMovies.add(movie.copy(matchScore = 0.2))
            }
        }
        
        return rankedMovies
    }
    
    companion object {
        private const val TAG = "RecommendationService"
        private const val GEMINI_API_KEY = "AIzaSyDs09ynNZPwIGQRlz3gU88PNQtntVOJTXE"
    }
}
