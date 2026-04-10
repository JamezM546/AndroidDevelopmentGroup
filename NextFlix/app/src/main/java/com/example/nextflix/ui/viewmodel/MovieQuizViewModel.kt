package com.example.nextflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.quiz.MovieQuizAnswer
import com.example.nextflix.data.quiz.MovieQuizStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieQuizViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val store = MovieQuizStore(application)

    private val _genre = MutableStateFlow("")
    val genre: StateFlow<String> = _genre.asStateFlow()

    private val _duration = MutableStateFlow("")
    val duration: StateFlow<String> = _duration.asStateFlow()

    private val _era = MutableStateFlow("")
    val era: StateFlow<String> = _era.asStateFlow()

    private val _ending = MutableStateFlow("")
    val ending: StateFlow<String> = _ending.asStateFlow()

    private val _specialEffects = MutableStateFlow("")
    val specialEffects: StateFlow<String> = _specialEffects.asStateFlow()

    private val _setting = MutableStateFlow("")
    val setting: StateFlow<String> = _setting.asStateFlow()

    private val _lastAnswer = MutableStateFlow<MovieQuizAnswer?>(null)
    val lastAnswer: StateFlow<MovieQuizAnswer?> = _lastAnswer.asStateFlow()

    private val _initialLoadDone = MutableStateFlow(false)
    val initialLoadDone: StateFlow<Boolean> = _initialLoadDone.asStateFlow()

    private val _validationError = MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = store.read()
            if (saved != null) {
                _genre.value = saved.genre
                _duration.value = saved.duration
                _era.value = saved.era
                _ending.value = saved.ending
                _specialEffects.value = saved.specialEffects
                _setting.value = saved.setting
                _lastAnswer.value = saved
            }
            _initialLoadDone.value = true
        }
    }

    fun setGenre(value: String) {
        _genre.value = value
    }

    fun setDuration(value: String) {
        _duration.value = value
    }

    fun setEra(value: String) {
        _era.value = value
    }

    fun setEnding(value: String) {
        _ending.value = value
    }

    fun setSpecialEffects(value: String) {
        _specialEffects.value = value
    }

    fun setSetting(value: String) {
        _setting.value = value
    }

    fun isComplete(): Boolean {
        return genre.value.isNotBlank() &&
                duration.value.isNotBlank() &&
                era.value.isNotBlank() &&
                ending.value.isNotBlank() &&
                specialEffects.value.isNotBlank() &&
                setting.value.isNotBlank()
    }

    suspend fun submitQuiz(): MovieQuizAnswer? {
        _validationError.value = ""
        
        if (!isComplete()) {
            _validationError.value = "Please answer all questions"
            return null
        }

        val answer = MovieQuizAnswer(
            genre = genre.value,
            duration = duration.value,
            era = era.value,
            ending = ending.value,
            specialEffects = specialEffects.value,
            setting = setting.value
        )

        store.write(answer)
        _lastAnswer.value = answer
        Log.d(TAG, "Movie quiz submitted: ${answer.getAnswerMap()}")
        return answer
    }

    fun resetQuiz() {
        viewModelScope.launch {
            store.clear()
            _genre.value = ""
            _duration.value = ""
            _era.value = ""
            _ending.value = ""
            _specialEffects.value = ""
            _setting.value = ""
            _lastAnswer.value = null
            _validationError.value = ""
        }
    }

    companion object {
        private const val TAG = "MovieQuiz"
    }
}
