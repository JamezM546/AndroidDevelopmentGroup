package com.example.nextflix.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.example.nextflix.R


class LoadingErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val spinner: ProgressBar
    private val errorText: TextView
    private val retryButton: MaterialButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_loading_error, this, true)
        spinner = findViewById(R.id.loadingSpinner)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
        gravity = Gravity.CENTER
        orientation = VERTICAL
    }

    fun showLoading() {
        visibility = View.VISIBLE
        spinner.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }

    fun showError(message: String, onRetry: () -> Unit) {
        visibility = View.VISIBLE
        spinner.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
        retryButton.visibility = View.VISIBLE
        retryButton.setOnClickListener { onRetry() }
    }

    fun hide() {
        visibility = View.GONE
    }
}