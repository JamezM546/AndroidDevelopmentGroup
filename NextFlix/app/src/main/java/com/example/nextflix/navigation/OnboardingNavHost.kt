package com.example.nextflix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nextflix.AppTab
import com.example.nextflix.ui.screens.ContentChoiceScreen
import com.example.nextflix.ui.screens.PersonalityQuizScreen
import com.example.nextflix.ui.screens.QuizSubmittedScreen
import com.example.nextflix.ui.screens.WelcomeScreen
import com.example.nextflix.ui.viewmodel.PersonalityQuizViewModel

@Composable
fun OnboardingNavHost(
    viewModel: PersonalityQuizViewModel,
    onCompleteOnboarding: (AppTab) -> Unit,
    coldActivityStart: Boolean
) {
    val navController = rememberNavController()

    LaunchedEffect(coldActivityStart) {
        if (coldActivityStart) {
            navController.navigate(AppRoutes.Welcome) {
                popUpTo(AppRoutes.Welcome) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Welcome
    ) {
        composable(AppRoutes.Welcome) {
            WelcomeScreen(
                onStartQuiz = { navController.navigate(AppRoutes.PersonalityQuiz) }
            )
        }
        composable(AppRoutes.PersonalityQuiz) {
            PersonalityQuizScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate(AppRoutes.QuizSubmitted) {
                        popUpTo(AppRoutes.PersonalityQuiz) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.QuizSubmitted) {
            QuizSubmittedScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate(AppRoutes.ContentChoice)}
            )
        }
        composable(AppRoutes.ContentChoice){
            ContentChoiceScreen(
                onChooseMovies = { onCompleteOnboarding(AppTab.MOVIE_QUIZ) },
                onChooseBooks = { onCompleteOnboarding(AppTab.BOOK_QUIZ)}
            )
        }
    }
}
