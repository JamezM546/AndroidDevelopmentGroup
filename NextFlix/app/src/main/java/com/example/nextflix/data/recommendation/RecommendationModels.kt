package com.example.nextflix.data.recommendation

import kotlinx.serialization.Serializable

@Serializable
enum class RecommendationContentType {
    MOVIE,
    BOOK
}

/**
 * Unified model for a recommended movie or book shown in lists and detail.
 */
data class RecommendationItem(
    val id: String,
    val contentType: RecommendationContentType,
    val title: String,
    val imageUrl: String?,
    val summary: String,
    /** Display string e.g. "8.4/10" or "4.5★"; null if unknown */
    val rating: String?
)

object SampleRecommendations {
    val movies: List<RecommendationItem> = listOf(
        RecommendationItem(
            id = "m1",
            contentType = RecommendationContentType.MOVIE,
            title = "The Grand Horizon",
            imageUrl = "https://picsum.photos/seed/movie1/400/600",
            summary = "An explorer returns home to mend old ties while chasing one last adventure across coastal towns.",
            rating = "8.4/10"
        ),
        RecommendationItem(
            id = "m2",
            contentType = RecommendationContentType.MOVIE,
            title = "Midnight in Arcadia",
            imageUrl = "https://picsum.photos/seed/movie2/400/600",
            summary = "A composer hears music no one else can, leading her through a city that shifts after dark.",
            rating = null
        ),
        RecommendationItem(
            id = "m3",
            contentType = RecommendationContentType.MOVIE,
            title = "Paper Planes & Thunder",
            imageUrl = "https://picsum.photos/seed/movie3/400/600",
            summary = "Childhood friends reunite to finish a film they abandoned a decade ago.",
            rating = "7.9/10"
        )
    )

    val books: List<RecommendationItem> = listOf(
        RecommendationItem(
            id = "b1",
            contentType = RecommendationContentType.BOOK,
            title = "The Salt Orchard",
            imageUrl = "https://picsum.photos/seed/book1/400/600",
            summary = "A botanist inherits a failing orchard and uncovers letters that rewrite her family story.",
            rating = "4.5★"
        ),
        RecommendationItem(
            id = "b2",
            contentType = RecommendationContentType.BOOK,
            title = "Letters Never Sent",
            imageUrl = "https://picsum.photos/seed/book2/400/600",
            summary = "Epistolary novel about two archivists falling in love one redacted line at a time.",
            rating = null
        ),
        RecommendationItem(
            id = "b3",
            contentType = RecommendationContentType.BOOK,
            title = "Orbit of Quiet Things",
            imageUrl = "https://picsum.photos/seed/book3/400/600",
            summary = "Quiet science fiction about a station where sound was outlawed—and the librarian who remembers songs.",
            rating = "4.2★"
        )
    )
}
