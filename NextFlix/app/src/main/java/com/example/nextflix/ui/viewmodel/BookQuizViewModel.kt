package com.example.nextflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.quiz.BookQuizAnswer
import com.example.nextflix.data.quiz.BookQuizStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookQuizViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val store = BookQuizStore(application)

    private val _genre = MutableStateFlow("")
    val genre: StateFlow<String> = _genre.asStateFlow()

    private val _mood = MutableStateFlow("")
    val mood: StateFlow<String> = _mood.asStateFlow()

    private val _length = MutableStateFlow("")
    val length: StateFlow<String> = _length.asStateFlow()

    private val _pace = MutableStateFlow("")
    val pace: StateFlow<String> = _pace.asStateFlow()

    private val _setting = MutableStateFlow("")
    val setting: StateFlow<String> = _setting.asStateFlow()

    private val _audience = MutableStateFlow("")
    val audience: StateFlow<String> = _audience.asStateFlow()

    private val _lastAnswer = MutableStateFlow<BookQuizAnswer?>(null)
    val lastAnswer: StateFlow<BookQuizAnswer?> = _lastAnswer.asStateFlow()

    private val _initialLoadDone = MutableStateFlow(false)
    val initialLoadDone: StateFlow<Boolean> = _initialLoadDone.asStateFlow()

    private val _validationError = MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = store.read()
            if (saved != null) {
                _genre.value = saved.genre
                _mood.value = saved.mood
                _length.value = saved.length
                _pace.value = saved.pace
                _setting.value = saved.setting
                _audience.value = saved.audience
                _lastAnswer.value = saved
            }
            _initialLoadDone.value = true
        }
    }

    fun setGenre(value: String) {
        _genre.value = value
    }

    fun setMood(value: String) {
        _mood.value = value
    }

    fun setLength(value: String) {
        _length.value = value
    }

    fun setPace(value: String) {
        _pace.value = value
    }

    fun setSetting(value: String) {
        _setting.value = value
    }

    fun setAudience(value: String) {
        _audience.value = value
    }

    fun isComplete(): Boolean {
        return genre.value.isNotBlank() &&
                mood.value.isNotBlank() &&
                length.value.isNotBlank() &&
                pace.value.isNotBlank() &&
                setting.value.isNotBlank() &&
                audience.value.isNotBlank()
    }

    suspend fun submitQuiz(): BookQuizAnswer? {
        _validationError.value = ""
        
        if (!isComplete()) {
            _validationError.value = "Please answer all questions"
            return null
        }

        val answer = BookQuizAnswer(
            genre = genre.value,
            mood = mood.value,
            length = length.value,
            pace = pace.value,
            setting = setting.value,
            audience = audience.value
        )

        store.write(answer)
        _lastAnswer.value = answer
        Log.d(TAG, "Book quiz submitted: ${answer.getAnswerMap()}")
        return answer
    }

    fun resetQuiz() {
        viewModelScope.launch {
            store.clear()
            _genre.value = ""
            _mood.value = ""
            _length.value = ""
            _pace.value = ""
            _setting.value = ""
            _audience.value = ""
            _lastAnswer.value = null
            _validationError.value = ""
        }
    }

    companion object {
        private const val TAG = "BookQuiz"
    }
}
