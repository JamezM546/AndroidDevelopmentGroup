package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.nextflix.R
import com.google.android.material.button.MaterialButton

class SubmitButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val button: MaterialButton

    init {
        LayoutInflater.from(context).inflate(R.layout.submit_button, this, true)
        button = findViewById(R.id.submitButton)
    }

    fun setOnSubmitClickListener(listener: () -> Unit) {
        button.setOnClickListener { listener() }
    }

    fun setEnabled(enabled: Boolean) {
        button.isEnabled = enabled
    }

    fun setLabel(text: String) {
        button.text = text
    }
}