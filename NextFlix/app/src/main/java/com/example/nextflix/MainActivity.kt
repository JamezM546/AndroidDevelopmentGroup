package com.example.nextflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nextflix.navigation.OnboardingNavHost
import com.example.nextflix.navigation.ResultsNavHost
import com.example.nextflix.ui.screens.MoviePreferenceQuizScreen
import com.example.nextflix.ui.theme.NextFlixTheme
import com.example.nextflix.ui.screens.BookPreferenceQuizScreen
import com.example.nextflix.ui.viewmodel.PersonalityQuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val coldActivityStart = savedInstanceState == null
        setContent {
            NextFlixTheme {
                val personalityVm: PersonalityQuizViewModel = viewModel()
                val initialLoadDone by personalityVm.initialLoadDone.collectAsStateWithLifecycle()
                val hasStoredProfile by personalityVm.hasStoredProfile.collectAsStateWithLifecycle()
                var showMainApp by rememberSaveable { mutableStateOf(false) }
                var startTab by remember { mutableStateOf(AppTab.HOME) }

                LaunchedEffect(initialLoadDone, hasStoredProfile) {
                    if (initialLoadDone && hasStoredProfile) {
                        showMainApp = true
                    }
                }

                when {
                    !initialLoadDone -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    showMainApp -> {
                        NextFlixApp(
                            personalityQuizViewModel = personalityVm,
                            initialTab = startTab
                        )
                    }
                    else -> {
                        OnboardingNavHost(
                            viewModel = personalityVm,
                            onCompleteOnboarding = { tab ->
                                startTab = tab
                                showMainApp = true
                            },
                            coldActivityStart = coldActivityStart
                        )
                    }
                }
            }
        }
    }
}

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MOVIE_QUIZ("Movie Quiz", Icons.Default.Movie),
    BOOK_QUIZ("Book Quiz", Icons.AutoMirrored.Filled.MenuBook),
    RESULTS("Results", Icons.Default.Star),
    FAVORITES("Favorites", Icons.Default.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextFlixApp(
    personalityQuizViewModel: PersonalityQuizViewModel,
    initialTab: AppTab = AppTab.HOME
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NextFlix",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeTabContent(personalityQuizViewModel = personalityQuizViewModel)
                AppTab.MOVIE_QUIZ -> MoviePreferenceQuizScreen(
                    onNavigateBack = { selectedTab = AppTab.HOME }
                )
                AppTab.BOOK_QUIZ -> BookPreferenceQuizScreen()
                AppTab.RESULTS -> ResultsNavHost()
                AppTab.FAVORITES -> PlaceholderScreen("Favorites", "Coming soon!")
            }
        }
    }
}

@Composable
fun HomeTabContent(
    personalityQuizViewModel: PersonalityQuizViewModel
) {
    val quizResult by personalityQuizViewModel.lastResult.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to NextFlix!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your ultimate destination for personalized movie and book recommendations.",
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (quizResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your personality quiz is saved and will help refine future picks.",
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
