package com.example.nextflix.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.example.nextflix.R

class SectionHeadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.section_heading, this, true)
        textView = findViewById(R.id.headingText)
    }

    fun setHeading(text: String) {
        textView.text = text
    }
}