package com.example.nextflix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                when (item.contentType) {
                    RecommendationContentType.MOVIE -> MovieResultCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                    RecommendationContentType.BOOK -> BookResultCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
