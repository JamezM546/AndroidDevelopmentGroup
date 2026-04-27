package com.example.nextflix.data.reaction

import com.example.nextflix.data.recommendation.RecommendationContentType
import kotlinx.serialization.Serializable

enum class Reaction { LIKED, DISLIKED }

@Serializable
data class ReactionEntry(
    val id: String,
    val contentType: RecommendationContentType,
    val title: String,
    val descriptor: String
)

@Serializable
data class ReactionState(
    val liked: List<ReactionEntry> = emptyList(),
    val disliked: List<ReactionEntry> = emptyList()
)

data class ReactionContext(
    val liked: List<ReactionEntry> = emptyList(),
    val disliked: List<ReactionEntry> = emptyList()
) {
    val isEmpty: Boolean get() = liked.isEmpty() && disliked.isEmpty()
}
