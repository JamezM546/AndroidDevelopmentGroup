package com.example.nextflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nextflix.data.reaction.Reaction
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem

@Composable
fun RecommendationResultCard(
    item: RecommendationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentReaction: Reaction? = null,
    onReact: ((Reaction?) -> Unit)? = null,
    isSaved: Boolean = false,
    onSaveToggle: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            PosterOrPlaceholder(item = item, modifier = Modifier.width(96.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (item.contentType) {
                            RecommendationContentType.MOVIE -> Icons.Default.Movie
                            RecommendationContentType.BOOK -> Icons.AutoMirrored.Filled.MenuBook
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.rating != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.rating,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row {
                        if (onReact != null) {
                            IconButton(
                                onClick = {
                                    onReact(if (currentReaction == Reaction.LIKED) null else Reaction.LIKED)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentReaction == Reaction.LIKED) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (currentReaction == Reaction.LIKED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onReact(if (currentReaction == Reaction.DISLIKED) null else Reaction.DISLIKED)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentReaction == Reaction.DISLIKED) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = if (currentReaction == Reaction.DISLIKED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (onSaveToggle != null) {
                            IconButton(
                                onClick = onSaveToggle,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieResultCard(
    item: RecommendationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentReaction: Reaction? = null,
    onReact: ((Reaction?) -> Unit)? = null,
    isSaved: Boolean = false,
    onSaveToggle: (() -> Unit)? = null
) {
    RecommendationResultCard(item = item, onClick = onClick, modifier = modifier, currentReaction = currentReaction, onReact = onReact, isSaved = isSaved, onSaveToggle = onSaveToggle)
}

@Composable
fun BookResultCard(
    item: RecommendationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentReaction: Reaction? = null,
    onReact: ((Reaction?) -> Unit)? = null,
    isSaved: Boolean = false,
    onSaveToggle: (() -> Unit)? = null
) {
    RecommendationResultCard(item = item, onClick = onClick, modifier = modifier, currentReaction = currentReaction, onReact = onReact, isSaved = isSaved, onSaveToggle = onSaveToggle)
}

@Composable
private fun PosterOrPlaceholder(
    item: RecommendationItem,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    if (item.imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .aspectRatio(2f / 3f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (item.contentType) {
                    RecommendationContentType.MOVIE -> Icons.Default.Movie
                    RecommendationContentType.BOOK -> Icons.AutoMirrored.Filled.MenuBook
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest)
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            modifier = modifier
                .aspectRatio(2f / 3f)
                .clip(shape),
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            error = placeholder
        )
    }
}
