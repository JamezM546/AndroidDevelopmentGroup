package com.example.nextflix.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextflix.data.reaction.Reaction
import com.example.nextflix.data.reaction.ReactionEntry
import com.example.nextflix.data.reaction.ReactionState
import com.example.nextflix.data.reaction.ReactionStore
import com.example.nextflix.data.recommendation.RecommendationContentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReactionViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ReactionStore(application)

    private val _state = MutableStateFlow(ReactionState())
    val state: StateFlow<ReactionState> = _state.asStateFlow()

    val likedIds: StateFlow<Set<String>> = _state
        .map { it.liked.map { e -> e.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val dislikedIds: StateFlow<Set<String>> = _state
        .map { it.disliked.map { e -> e.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            _state.value = store.read()
        }
    }

    fun reactionFor(id: String): Reaction? {
        val s = _state.value
        return when {
            s.liked.any { it.id == id } -> Reaction.LIKED
            s.disliked.any { it.id == id } -> Reaction.DISLIKED
            else -> null
        }
    }

    fun react(
        id: String,
        contentType: RecommendationContentType,
        title: String,
        descriptor: String,
        reaction: Reaction?
    ) {
        val current = _state.value
        val entry = ReactionEntry(id = id, contentType = contentType, title = title, descriptor = descriptor)
        val newState = when (reaction) {
            Reaction.LIKED -> ReactionState(
                liked = current.liked.filterNot { it.id == id } + entry,
                disliked = current.disliked.filterNot { it.id == id }
            )
            Reaction.DISLIKED -> ReactionState(
                liked = current.liked.filterNot { it.id == id },
                disliked = current.disliked.filterNot { it.id == id } + entry
            )
            null -> ReactionState(
                liked = current.liked.filterNot { it.id == id },
                disliked = current.disliked.filterNot { it.id == id }
            )
        }
        _state.value = newState
        viewModelScope.launch { store.write(newState) }
    }

    suspend fun snapshot(): ReactionState = store.read()
}
