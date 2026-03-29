package com.example.nextflix.data.personality

import kotlinx.serialization.Serializable

enum class PersonalityDimension {
    LEISURE_STYLE,
    STRUCTURE,
    PACING,
    STORY_TONE,
    SETTING,
    EMOTIONAL_GOAL,
    SOCIAL_CONTEXT,
    COMMITMENT
}

data class PersonalityQuizOption(
    val id: String,
    val label: String
)

data class PersonalityQuizQuestion(
    val id: String,
    val dimension: PersonalityDimension,
    val prompt: String,
    val options: List<PersonalityQuizOption>
)

@Serializable
data class PersonalityQuizResult(
    val answers: Map<String, String>,
    val completedAtEpochMillis: Long = System.currentTimeMillis()
) {
    fun selectedOptionId(questionId: String): String? = answers[questionId]
}
