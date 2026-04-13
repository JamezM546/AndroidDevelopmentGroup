package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.example.nextflix.R
import com.google.android.material.card.MaterialCardView

class QuestionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.question_card, this, true)
        textView = findViewById(R.id.questionText)
        radius = 16f
        cardElevation = 4f
    }

    fun setQuestion(text: String) {
        textView.text = text
    }

    fun setError(hasError: Boolean) {
        strokeColor = if (hasError) {
            context.getColor(com.google.android.material.R.color.design_error)
        } else {
            context.getColor(android.R.color.transparent)
        }
        strokeWidth = if (hasError) 4 else 0
    }
}