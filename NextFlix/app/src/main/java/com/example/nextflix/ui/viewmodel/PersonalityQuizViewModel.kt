package com.example.nextflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.personality.PersonalityQuizCatalog
import com.example.nextflix.data.personality.PersonalityQuizResult
import com.example.nextflix.data.personality.PersonalityQuizStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonalityQuizViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val store = PersonalityQuizStore(application)

    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers: StateFlow<Map<String, String>> = _answers.asStateFlow()

    private val _lastResult = MutableStateFlow<PersonalityQuizResult?>(null)
    val lastResult: StateFlow<PersonalityQuizResult?> = _lastResult.asStateFlow()

    private val _initialLoadDone = MutableStateFlow(false)
    val initialLoadDone: StateFlow<Boolean> = _initialLoadDone.asStateFlow()

    /** True when a saved profile was loaded from storage (cold start). */
    private val _hasStoredProfile = MutableStateFlow(false)
    val hasStoredProfile: StateFlow<Boolean> = _hasStoredProfile.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = store.read()
            if (saved != null) {
                _lastResult.value = saved
                _answers.value = saved.answers
                _hasStoredProfile.value = true
            }
            _initialLoadDone.value = true
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        _answers.update { current -> current + (questionId to optionId) }
    }

    fun isComplete(): Boolean {
        val needed = PersonalityQuizCatalog.questions.size
        return _answers.value.size >= needed &&
            PersonalityQuizCatalog.questions.all { q -> _answers.value.containsKey(q.id) }
    }

    suspend fun submitQuiz(): PersonalityQuizResult? {
        if (!isComplete()) return null
        val result = PersonalityQuizResult(answers = _answers.value.toMap())
        store.write(result)
        _lastResult.value = result
        Log.d(TAG, "Personality quiz submitted: ${result.answers}")
        return result
    }

    companion object {
        private const val TAG = "PersonalityQuiz"
    }
}
