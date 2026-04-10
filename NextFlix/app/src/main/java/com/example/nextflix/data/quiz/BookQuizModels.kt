package com.example.nextflix.data.quiz

import kotlinx.serialization.Serializable

@Serializable
data class BookQuizAnswer(
    val genre: String,
    val mood: String,
    val length: String,
    val pace: String,
    val setting: String,
    val audience: String,
    val completedAtEpochMillis: Long = System.currentTimeMillis()
) {
    fun isComplete(): Boolean {
        return genre.isNotBlank() &&
                mood.isNotBlank() &&
                length.isNotBlank() &&
                pace.isNotBlank() &&
                setting.isNotBlank() &&
                audience.isNotBlank()
    }

    fun getAnswerMap(): Map<String, String> = mapOf(
        "genre" to genre,
        "mood" to mood,
        "length" to length,
        "pace" to pace,
        "setting" to setting,
        "audience" to audience
    )
}
