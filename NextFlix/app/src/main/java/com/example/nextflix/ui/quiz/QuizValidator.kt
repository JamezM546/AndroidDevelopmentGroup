package com.example.nextflix.ui.quiz

object QuizValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val unansweredIds: List<String>
    )

    fun validate(questions: List<QuizQuestion>): ValidationResult {
        val unanswered = questions
            .filter { it.isRequired && it.selectedAnswer.isNullOrBlank() }
            .map { it.id }

        return ValidationResult(
            isValid = unanswered.isEmpty(),
            unansweredIds = unanswered
        )
    }
}