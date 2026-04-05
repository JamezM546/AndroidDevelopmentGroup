package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.nextflix.R

class QuestionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.question_card, this, true)
        textView = findViewById(R.id.questionText)
    }

    fun setQuestion(text: String) {
        textView.text = text
    }
}