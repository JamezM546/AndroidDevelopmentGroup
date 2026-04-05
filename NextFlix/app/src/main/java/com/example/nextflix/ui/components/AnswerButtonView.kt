package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.graphics.Color
import com.example.nextflix.R



class AnswerButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val button: Button
    private var isSelectedState = false

    init {
        LayoutInflater.from(context).inflate(R.layout.answerButton, this, true)
        button = findViewById(R.id.answerButton)

        button.setOnClickListener {
            toggleSelection()
        }
    }

    fun setText(text: String) {
        button.text = text
    }

    fun setOnAnswerClick(listener: () -> Unit) {
        button.setOnClickListener {
            toggleSelection()
            listener()
        }
    }

    private fun toggleSelection() {
        isSelectedState = !isSelectedState
        updateUI()
    }

    private fun updateUI() {
        button.setBackgroundColor(
            if (isSelectedState) Color.BLUE else Color.LTGRAY
        )
    }

    fun isSelected(): Boolean {
        return isSelectedState
    }
}