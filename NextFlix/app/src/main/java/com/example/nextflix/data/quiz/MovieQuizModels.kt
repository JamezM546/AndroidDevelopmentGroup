package com.example.nextflix.data.quiz

import kotlinx.serialization.Serializable

@Serializable
data class MovieQuizAnswer(
    val genre: String,
    val duration: String,
    val era: String,
    val ending: String,
    val specialEffects: String,
    val setting: String,
    val completedAtEpochMillis: Long = System.currentTimeMillis()
) {
    fun isComplete(): Boolean {
        return genre.isNotBlank() &&
                duration.isNotBlank() &&
                era.isNotBlank() &&
                ending.isNotBlank() &&
                specialEffects.isNotBlank() &&
                setting.isNotBlank()
    }

    fun getAnswerMap(): Map<String, String> = mapOf(
        "genre" to genre,
        "duration" to duration,
        "era" to era,
        "ending" to ending,
        "specialEffects" to specialEffects,
        "setting" to setting
    )
}
