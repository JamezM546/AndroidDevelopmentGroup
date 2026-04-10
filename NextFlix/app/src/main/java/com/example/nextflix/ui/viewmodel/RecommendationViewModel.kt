package com.example.nextflix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem
import com.example.nextflix.data.recommendation.SampleRecommendations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RecommendationViewModel : ViewModel() {

    private val _contentFilter = MutableStateFlow(RecommendationContentType.MOVIE)
    val contentFilter: StateFlow<RecommendationContentType> = _contentFilter

    // Live movie results from TMDB API
    private val _movieResults = MutableStateFlow<List<RecommendationItem>>(emptyList())

    val visibleItems: StateFlow<List<RecommendationItem>> = _contentFilter
        .map { filter ->
            when (filter) {
                RecommendationContentType.MOVIE -> {
                    val live = _movieResults.value
                    if (live.isNotEmpty()) live else SampleRecommendations.movies
                }
                RecommendationContentType.BOOK -> SampleRecommendations.books
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SampleRecommendations.movies
        )

    private val _selectedDetail = MutableStateFlow<RecommendationItem?>(null)
    val selectedDetail: StateFlow<RecommendationItem?> = _selectedDetail

    fun setContentFilter(type: RecommendationContentType) {
        _contentFilter.value = type
    }

    fun setMovieResults(movies: List<RecommendationItem>) {
        _movieResults.value = movies
        // Refresh visible items by re-emitting the filter
        _contentFilter.update { it }
    }

    fun openDetail(item: RecommendationItem) {
        _selectedDetail.value = item
    }

    fun clearDetail() {
        _selectedDetail.update { null }
    }
}
