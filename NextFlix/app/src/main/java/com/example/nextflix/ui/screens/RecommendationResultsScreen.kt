package com.example.nextflix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nextflix.data.reaction.Reaction
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem
import com.example.nextflix.ui.components.BookResultCard
import com.example.nextflix.ui.components.MovieResultCard

@Composable
fun RecommendationResultsScreen(
    items: List<RecommendationItem>,
    contentFilter: RecommendationContentType,
    onContentFilterChange: (RecommendationContentType) -> Unit,
    onItemClick: (RecommendationItem) -> Unit,
    modifier: Modifier = Modifier,
    personalityChanged: Boolean = false,
    reactionFor: ((String) -> Reaction?)? = null,
    onReact: ((RecommendationItem, Reaction?) -> Unit)? = null,
    savedIds: Set<String> = emptySet(),
    onSaveToggle: ((RecommendationItem) -> Unit)? = null
) {
    var bannerDismissed by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Text(
            text = "Browse picks tailored to your taste. Switch between movies and books.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = contentFilter == RecommendationContentType.MOVIE,
                onClick = { onContentFilterChange(RecommendationContentType.MOVIE) },
                label = { Text("Movies") }
            )
            FilterChip(
                selected = contentFilter == RecommendationContentType.BOOK,
                onClick = { onContentFilterChange(RecommendationContentType.BOOK) },
                label = { Text("Books") }
            )
        }

        if (personalityChanged && !bannerDismissed && items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Your personality changed — re-run the movie or book quiz to refresh recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { bannerDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results yet. Generate movie or book recommendations to populate this tab.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    when (item.contentType) {
                        RecommendationContentType.MOVIE -> MovieResultCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            currentReaction = reactionFor?.invoke(item.id),
                            onReact = if (onReact != null) { r -> onReact(item, r) } else null,
                            isSaved = item.id in savedIds,
                            onSaveToggle = if (onSaveToggle != null) { { onSaveToggle(item) } } else null
                        )
                        RecommendationContentType.BOOK -> BookResultCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            currentReaction = reactionFor?.invoke(item.id),
                            onReact = if (onReact != null) { r -> onReact(item, r) } else null,
                            isSaved = item.id in savedIds,
                            onSaveToggle = if (onSaveToggle != null) { { onSaveToggle(item) } } else null
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}
