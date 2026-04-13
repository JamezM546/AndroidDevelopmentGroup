package com.example.nextflix.ui.quiz

import androidx.privacysandbox.tools.core.validator.ValidationResult
import com.example.nextflix.ui.components.QuestionCardView

object ValidationUiHelper {

    fun applyValidation(
        result: QuizValidator.ValidationResult,
        cardMap: Map<String, QuestionCardView>
    ) {

        cardMap.values.forEach { it.setError(false) }


        result.unansweredIds.forEach { id ->
            cardMap[id]?.setError(true)
        }
    }
}