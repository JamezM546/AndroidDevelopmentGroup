package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.nextflix.R

class SectionHeadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val headingText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.section_heading, this, true)
        headingText = findViewById(R.id.headingText)
    }

    fun setTitle(text: String) {
        headingText.text = text
    }
}