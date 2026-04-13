package com.example.nextflix.ui.recommendations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.ui.common.UiState
import kotlinx.coroutines.launch

class RecommendationViewModel : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<String>>>(UiState.Idle)
    val uiState: LiveData<UiState<List<String>>> = _uiState

    fun fetchRecommendations() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {

                val results = listOf("Result 1", "Result 2")
                _uiState.value = UiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Something went wrong. Please try again.")
            }
        }
    }
}