package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.nextflix.R

class SubmitButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val button: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.submit_button, this, true)
        button = findViewById(R.id.submitButton)
    }

    fun setOnSubmitClick(listener: () -> Unit) {
        button.setOnClickListener {
            listener()
        }
    }
}