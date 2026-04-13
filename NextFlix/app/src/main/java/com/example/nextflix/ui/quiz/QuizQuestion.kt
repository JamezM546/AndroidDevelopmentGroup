package com.example.nextflix.ui.quiz
data class QuizQuestion(
    val id: String,
    val text: String,
    val isRequired: Boolean = true,
    var selectedAnswer: String? = null
)