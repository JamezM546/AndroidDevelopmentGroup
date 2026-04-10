package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.nextflix.R
import com.google.android.material.button.MaterialButton


class AnswerButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val button: MaterialButton

    init {
        LayoutInflater.from(context).inflate(R.layout.answer_button, this, true)
        button = findViewById(R.id.answerButton)
    }

    fun setAnswer(text: String) {
        button.text = text
    }

    fun setOnAnswerClickListener(listener: () -> Unit) {
        button.setOnClickListener { listener() }
    }

    fun setSelected(selected: Boolean) {
        button.isChecked = selected
    }
}