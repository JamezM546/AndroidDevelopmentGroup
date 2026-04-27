package com.example.nextflix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RecommendationViewModel : ViewModel() {

    private val _contentFilter = MutableStateFlow(RecommendationContentType.MOVIE)
    val contentFilter: StateFlow<RecommendationContentType> = _contentFilter

    private val _movieResults = MutableStateFlow<List<RecommendationItem>>(emptyList())
    private val _bookResults = MutableStateFlow<List<RecommendationItem>>(emptyList())

    val visibleItems: StateFlow<List<RecommendationItem>> = combine(
        _contentFilter,
        _movieResults,
        _bookResults
    ) { filter, movieResults, bookResults ->
            when (filter) {
                RecommendationContentType.MOVIE -> movieResults
                RecommendationContentType.BOOK -> bookResults
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setContentFilter(type: RecommendationContentType) {
        _contentFilter.value = type
    }

    fun setMovieResults(movies: List<RecommendationItem>) {
        _movieResults.value = movies
    }

    fun setBookResults(books: List<RecommendationItem>) {
        _bookResults.value = books
    }

    fun findItem(contentType: RecommendationContentType, id: String): RecommendationItem? {
        val source = when (contentType) {
            RecommendationContentType.MOVIE -> _movieResults.value
            RecommendationContentType.BOOK -> _bookResults.value
        }
        return source.firstOrNull { it.id == id }
    }
}
