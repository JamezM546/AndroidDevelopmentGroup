package com.example.nextflix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nextflix.ui.screens.RecommendationDetailScreen
import com.example.nextflix.ui.screens.RecommendationResultsScreen
import com.example.nextflix.ui.viewmodel.RecommendationViewModel

private object ResultsRoutes {
    const val List = "recommendation_list"
    const val Detail = "recommendation_detail"
}

@Composable
fun ResultsNavHost(
    viewModel: RecommendationViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ResultsRoutes.List
    ) {
        composable(ResultsRoutes.List) {
            val items by viewModel.visibleItems.collectAsStateWithLifecycle()
            val contentFilter by viewModel.contentFilter.collectAsStateWithLifecycle()
            RecommendationResultsScreen(
                items = items,
                contentFilter = contentFilter,
                onContentFilterChange = { viewModel.setContentFilter(it) },
                onItemClick = { item ->
                    viewModel.openDetail(item)
                    navController.navigate(ResultsRoutes.Detail)
                }
            )
        }
        composable(ResultsRoutes.Detail) {
            val selectedDetail by viewModel.selectedDetail.collectAsStateWithLifecycle()
            val item = selectedDetail
            if (item == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(ResultsRoutes.List, inclusive = false)
                }
            } else {
                RecommendationDetailScreen(
                    item = item,
                    onBack = {
                        viewModel.clearDetail()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
