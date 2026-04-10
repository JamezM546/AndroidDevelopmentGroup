package com.example.nextflix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nextflix.ui.viewmodel.BookQuizViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class BookQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val fieldSetter: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPreferenceQuizScreen(
    onNavigateBack: () -> Unit = {},
    onQuizComplete: () -> Unit = {},
    viewModel: BookQuizViewModel = viewModel()
) {
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val mood by viewModel.mood.collectAsStateWithLifecycle()
    val length by viewModel.length.collectAsStateWithLifecycle()
    val pace by viewModel.pace.collectAsStateWithLifecycle()
    val setting by viewModel.setting.collectAsStateWithLifecycle()
    val audience by viewModel.audience.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val questions = remember {
        listOf(
            BookQuizQuestion(
                id = 1,
                question = "What genre are you most interested in?",
                options = listOf("Fantasy", "Mystery", "Romance", "Science Fiction", "Historical", "Thriller"),
                fieldSetter = viewModel::setGenre
            ),
            BookQuizQuestion(
                id = 2,
                question = "What kind of mood do you want?",
                options = listOf("Light and fun", "Dark and intense", "Emotional", "Thought-provoking"),
                fieldSetter = viewModel::setMood
            ),
            BookQuizQuestion(
                id = 3,
                question = "What length do you prefer?",
                options = listOf("Short (< 300 pages)", "Medium (300-500)", "Long (500+ pages)", "No preference"),
                fieldSetter = viewModel::setLength
            ),
            BookQuizQuestion(
                id = 4,
                question = "What reading pace do you like?",
                options = listOf("Fast-paced", "Balanced", "Slow and detailed"),
                fieldSetter = viewModel::setPace
            ),
            BookQuizQuestion(
                id = 5,
                question = "What setting sounds most interesting?",
                options = listOf("Modern day", "Historical", "Fantasy world", "Space / futuristic"),
                fieldSetter = viewModel::setSetting
            ),
            BookQuizQuestion(
                id = 6,
                question = "Who is this book for?",
                options = listOf("Just me", "Me and friends", "School / class", "Gift for someone"),
                fieldSetter = viewModel::setAudience
            )
        )
    }
    
    val selectedAnswers = remember {
        mutableStateMapOf<Int, String>().apply {
            if (genre.isNotBlank()) this[1] = genre
            if (mood.isNotBlank()) this[2] = mood
            if (length.isNotBlank()) this[3] = length
            if (pace.isNotBlank()) this[4] = pace
            if (setting.isNotBlank()) this[5] = setting
            if (audience.isNotBlank()) this[6] = audience
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Book Preferences",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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
            
            Text(
                text = "${selectedAnswers.size} of ${questions.size} questions answered",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Answer a few questions to help us match you with books you'll enjoy.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            questions.forEach { question ->
                BookQuestionCard(
                    question = question,
                    selectedAnswer = selectedAnswers[question.id],
                    onAnswerSelected = { answer ->
                        selectedAnswers[question.id] = answer
                        question.fieldSetter(answer)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (validationError.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = validationError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (selectedAnswers.size == questions.size && questions.isNotEmpty()) {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.submitQuiz()
                            if (validationError.isEmpty()) {
                                onQuizComplete()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Get Book Recommendations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BookQuestionCard(
    question: BookQuizQuestion,
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
            
            Text(
                text = question.question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            question.options.forEachIndexed { index, option ->
                BookAnswerOption(
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
fun BookAnswerOption(
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
