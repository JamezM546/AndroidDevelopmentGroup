package com.example.nextflix.data.api

import android.util.Log
import com.example.nextflix.BuildConfig
import com.example.nextflix.data.models.Book
import com.example.nextflix.data.models.Movie
import com.example.nextflix.data.personality.PersonalityPreferenceMapper
import com.example.nextflix.data.quiz.BookQuizAnswer
import com.example.nextflix.data.quiz.MovieQuizAnswer
import com.example.nextflix.data.personality.PersonalityQuizResult
import com.example.nextflix.data.reaction.ReactionContext
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
        availableBooks: List<Book>,
        reactionContext: ReactionContext = ReactionContext()
    ): Result<List<Book>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Build a prompt for the AI
            val prompt = buildBookRecommendationPrompt(bookQuizAnswer, personalityProfile, availableBooks, reactionContext)
            
            // Call Free AI API
            val ranking = callAIAPI(prompt, availableBooks.map { it.id })
            
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
        availableMovies: List<Movie>,
        reactionContext: ReactionContext = ReactionContext()
    ): Result<List<Movie>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Build a prompt for the AI
            val prompt = buildMovieRecommendationPrompt(movieQuizAnswer, personalityProfile, availableMovies, reactionContext)
            
            // Call Free AI API
            val ranking = callAIAPI(prompt, availableMovies.map { it.id })
            
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
        availableBooks: List<Book>,
        reactionContext: ReactionContext
    ): String {
        val mappedPreferences = PersonalityPreferenceMapper.map(personalityProfile)
        val rawPersonalityAnswers = personalityProfile?.answers
            ?.entries
            ?.joinToString(", ") { "${it.key}: ${it.value}" }
            ?: "No personality answers submitted"

        val reactionBlock = buildReactionPromptBlock(reactionContext)

        return """
            You are a book recommendation expert. Based on the user's preferences, rank these books from best to least suitable.

            User's Book Preferences:
            - Genre: ${quizValueOrFallback(bookQuiz.genre)}
            - Mood: ${quizValueOrFallback(bookQuiz.mood)}
            - Length: ${quizValueOrFallback(bookQuiz.length)}
            - Pace: ${quizValueOrFallback(bookQuiz.pace)}
            - Setting: ${quizValueOrFallback(bookQuiz.setting)}
            - Audience: ${quizValueOrFallback(bookQuiz.audience)}

            Raw Personality Answers: $rawPersonalityAnswers
            ${PersonalityPreferenceMapper.toPromptBlock(mappedPreferences)}
            $reactionBlock
            Available Books (ID: Title - Author):
            ${availableBooks.joinToString("\n") { "${it.id}: ${it.title} by ${it.author}" }}

            Return ONLY a JSON array of book IDs in order of recommendation, like: ["id1", "id2", "id3"]
            Do not include any other text.
        """.trimIndent()
    }
    
    private fun buildMovieRecommendationPrompt(
        movieQuiz: MovieQuizAnswer,
        personalityProfile: PersonalityQuizResult?,
        availableMovies: List<Movie>,
        reactionContext: ReactionContext
    ): String {
        val mappedPreferences = PersonalityPreferenceMapper.map(personalityProfile)
        val rawPersonalityAnswers = personalityProfile?.answers
            ?.entries
            ?.joinToString(", ") { "${it.key}: ${it.value}" }
            ?: "No personality answers submitted"

        val reactionBlock = buildReactionPromptBlock(reactionContext)

        return """
            You are a movie recommendation expert. Based on the user's preferences, rank these movies from best to least suitable.

            User's Movie Preferences:
            - Genre: ${quizValueOrFallback(movieQuiz.genre)}
            - Duration: ${quizValueOrFallback(movieQuiz.duration)}
            - Era: ${quizValueOrFallback(movieQuiz.era)}
            - Ending: ${quizValueOrFallback(movieQuiz.ending)}
            - Special Effects: ${quizValueOrFallback(movieQuiz.specialEffects)}
            - Setting: ${quizValueOrFallback(movieQuiz.setting)}

            Raw Personality Answers: $rawPersonalityAnswers
            ${PersonalityPreferenceMapper.toPromptBlock(mappedPreferences)}
            $reactionBlock
            Available Movies (ID: Title - Year):
            ${availableMovies.joinToString("\n") { "${it.id}: ${it.title} (${it.releaseYear ?: "N/A"})" }}

            Return ONLY a JSON array of movie IDs in order of recommendation, like: ["id1", "id2", "id3"]
            Do not include any other text.
        """.trimIndent()
    }

    private fun buildReactionPromptBlock(reactionContext: ReactionContext): String {
        if (reactionContext.isEmpty) return ""
        val lines = mutableListOf("", "User's Past Reactions:")
        if (reactionContext.liked.isNotEmpty()) {
            val liked = reactionContext.liked.joinToString(", ") { "${it.title} (${it.descriptor})" }
            lines.add("Liked: $liked")
        }
        if (reactionContext.disliked.isNotEmpty()) {
            val disliked = reactionContext.disliked.joinToString(", ") { "${it.title} (${it.descriptor})" }
            lines.add("Disliked: $disliked")
        }
        lines.add("Rank items more similar to the Liked entries higher, and rank items similar to the Disliked entries lower or last. The Disliked entries themselves have already been filtered out — do not reintroduce them.")
        return lines.joinToString("\n")
    }
    
    private fun callAIAPI(prompt: String, expectedIds: List<String>): List<String> {
        if (expectedIds.isEmpty()) return emptyList()

        val requestBody = buildOpenRouterRequestBody(prompt).toString()
        try {
            val responseText = postOpenRouterRequest(requestBody)
            val ranking = parseOpenAIFormatRanking(responseText)
            if (ranking.isNotEmpty()) {
                return normalizeRanking(ranking, expectedIds)
            }
            Log.w(TAG, "AI returned no parseable ranking")
        } catch (e: Exception) {
            Log.e(TAG, "Failed AI attempt", e)
        }
        return expectedIds
    }

    private fun buildOpenRouterRequestBody(prompt: String): JSONObject {
        val requestBody = JSONObject()
        requestBody.put("model", "nvidia/nemotron-3-super-120b-a12b:free")
        
        val messagesArray = JSONArray()
        val messageObj = JSONObject()
        messageObj.put("role", "user")
        messageObj.put("content", prompt)
        messagesArray.put(messageObj)
        
        requestBody.put("messages", messagesArray)
        return requestBody
    }

    private fun postOpenRouterRequest(requestBody: String): String {
        val endpoint = "https://openrouter.ai/api/v1/chat/completions"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        return try {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (responseCode !in 200..299) {
                val snippet = responseBody.take(500)
                throw IllegalStateException("AI HTTP $responseCode. Response: $snippet")
            }

            responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun parseOpenAIFormatRanking(responseText: String): List<String> {
        val responseJson = JSONObject(responseText)
        val choices = responseJson.optJSONArray("choices") ?: return emptyList()

        for (i in 0 until choices.length()) {
            val choice = choices.optJSONObject(i) ?: continue
            val message = choice.optJSONObject("message") ?: continue
            val text = message.optString("content")?.trim().orEmpty()
            if (text.isBlank()) continue
            val parsed = parseRankingArray(text)
            if (parsed.isNotEmpty()) return parsed
        }

        return emptyList()
    }

    private fun parseRankingArray(rawText: String): List<String> {
        val jsonText = extractJsonArrayText(rawText)
        return try {
            val jsonArray = JSONArray(jsonText)
            val ranking = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                ranking.add(jsonArray.getString(i))
            }
            ranking
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun normalizeRanking(ranking: List<String>, expectedIds: List<String>): List<String> {
        val expectedSet = expectedIds.toSet()
        val filtered = ranking.filter { it in expectedSet }.distinct()
        if (filtered.isEmpty()) return expectedIds

        val filteredSet = filtered.toSet()
        val missing = expectedIds.filterNot { it in filteredSet }
        return filtered + missing
    }

    private fun extractJsonArrayText(rawText: String): String {
        val fencedJson = Regex("^```(?:json)?\\s*([\\s\\S]*?)\\s*```$").find(rawText)
        return fencedJson?.groupValues?.get(1)?.trim() ?: rawText
    }

    private fun quizValueOrFallback(value: String): String {
        return if (value.isBlank()) "No preference provided" else value
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
        
    }
}
