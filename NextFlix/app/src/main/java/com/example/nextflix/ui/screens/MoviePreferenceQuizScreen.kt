package com.example.nextflix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextflix.ui.theme.NextFlixTheme

// Placeholder data classes for UI only
data class MovieQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePreferenceQuizScreen(
    onNavigateBack: () -> Unit = {},
    onQuizComplete: () -> Unit = {}
) {
    // State to track selected answers
    val selectedAnswers = remember { mutableStateMapOf<Int, String>() }
    val scrollState = rememberScrollState()
    
    // Placeholder movie quiz questions
    val questions = remember {
        listOf(
            MovieQuizQuestion(
                id = 1,
                question = "What's your favorite movie genre?",
                options = listOf("Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Romance")
            ),
            MovieQuizQuestion(
                id = 2,
                question = "How long do you prefer movies to be?",
                options = listOf("Short (< 90 min)", "Medium (90-120 min)", "Long (> 120 min)", "No preference")
            ),
            MovieQuizQuestion(
                id = 3,
                question = "What movie era do you enjoy most?",
                options = listOf("Classic (before 1980)", "Golden Age (1980-2000)", "Modern (2000-2015)", "Recent (2015+)")
            ),
            MovieQuizQuestion(
                id = 4,
                question = "Do you prefer movies with happy or sad endings?",
                options = listOf("Happy endings", "Sad/bittersweet endings", "Unexpected twists", "No preference")
            ),
            MovieQuizQuestion(
                id = 5,
                question = "How important are special effects to you?",
                options = listOf("Very important", "Somewhat important", "Not important", "Depends on the movie")
            ),
            MovieQuizQuestion(
                id = 6,
                question = "What's your ideal movie setting?",
                options = listOf("Real world", "Fantasy world", "Future/Space", "Historical period", "Anywhere interesting")
            )
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Movie Preferences",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { if (questions.isEmpty()) 0f else selectedAnswers.size.toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            // Progress text
            Text(
                text = "${selectedAnswers.size} of ${questions.size} questions answered",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Questions
            questions.forEach { question ->
                MovieQuestionCard(
                    question = question,
                    selectedAnswer = selectedAnswers[question.id],
                    onAnswerSelected = { answer ->
                        selectedAnswers[question.id] = answer
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Submit button
            AnimatedVisibility(visible = selectedAnswers.size == questions.size && questions.isNotEmpty()) {
                Button(
                    onClick = onQuizComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Get Movie Recommendations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Spacer at bottom
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MovieQuestionCard(
    question: MovieQuizQuestion,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Question number badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Question ${question.id}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Question text
            Text(
                text = question.question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Answer options
            question.options.forEachIndexed { index, option ->
                MovieAnswerOption(
                    text = option,
                    isSelected = selectedAnswer == option,
                    onClick = { onAnswerSelected(option) }
                )
                if (index < question.options.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MovieAnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieQuizPreview() {
    NextFlixTheme {
        MoviePreferenceQuizScreen()
    }
}
