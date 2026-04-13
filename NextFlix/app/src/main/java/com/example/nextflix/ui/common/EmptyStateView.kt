package com.example.nextflix.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.example.nextflix.R


class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val retryButton: MaterialButton
    private val backButton: MaterialButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true)
        retryButton = findViewById(R.id.retryButton)
        backButton = findViewById(R.id.backButton)
        gravity = Gravity.CENTER
        orientation = VERTICAL
    }

    fun show(onRetry: () -> Unit, onBack: () -> Unit) {
        visibility = View.VISIBLE
        retryButton.setOnClickListener { onRetry() }
        backButton.setOnClickListener { onBack() }
    }

    fun hide() {
        visibility = View.GONE
    }
}