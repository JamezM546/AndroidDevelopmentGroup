package com.example.nextflix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BookPreferenceQuizScreen() {
    var selectedGenre by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("") }
    var selectedLength by remember { mutableStateOf("") }
    var selectedPace by remember { mutableStateOf("") }
    var submitMessage by remember { mutableStateOf("") }
    var selectedSetting by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Book Preference Quiz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Answer a few questions to help us match you with books you may enjoy.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        QuizQuestion(
            question = "1. What genre are you most interested in?",
            options = listOf("Fantasy", "Mystery", "Romance", "Science Fiction"),
            selectedOption = selectedGenre,
            onOptionSelected = { selectedGenre = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuizQuestion(
            question = "2. What kind of mood do you want?",
            options = listOf("Light and fun", "Dark and intense", "Emotional", "Thought-provoking"),
            selectedOption = selectedMood,
            onOptionSelected = { selectedMood = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuizQuestion(
            question = "3. What length do you prefer?",
            options = listOf("Short", "Medium", "Long"),
            selectedOption = selectedLength,
            onOptionSelected = { selectedLength = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuizQuestion(
            question = "4. What reading pace do you like?",
            options = listOf("Fast-paced", "Balanced", "Slow and detailed"),
            selectedOption = selectedPace,
            onOptionSelected = { selectedPace = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuizQuestion(
            question = "5. What setting sounds most interesting?",
            options = listOf("Modern day", "Historical", "Fantasy world", "Space / futuristic"),
            selectedOption = selectedSetting,
            onOptionSelected = { selectedSetting = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuizQuestion(
            question = "6. Who is this book for?",
            options = listOf("Just me", "Me and friends", "School / class", "Gift for someone"),
            selectedOption = selectedAudience,
            onOptionSelected = { selectedAudience = it }
        )



        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                submitMessage =
                    if (selectedGenre.isNotEmpty() &&
                        selectedMood.isNotEmpty() &&
                        selectedLength.isNotEmpty() &&
                        selectedPace.isNotEmpty() &&
                        selectedSetting.isNotEmpty() &&
                        selectedAudience.isNotEmpty()
                    ) {
                        "Book quiz UI ready for future logic hookup."
                    } else {
                        "Please answer all questions."
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }

        if (submitMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = submitMessage,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuizQuestion(
    question: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(
            text = question,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { option ->
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { onOptionSelected(option) }
                    )
                    Text(
                        text = option,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}