package com.example.nextflix.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.nextflix.data.recommendation.RecommendationContentType
import com.example.nextflix.data.recommendation.RecommendationItem
import com.example.nextflix.ui.screens.RecommendationDetailScreen
import com.example.nextflix.ui.screens.RecommendationResultsScreen
import com.example.nextflix.ui.viewmodel.RecommendationViewModel

private object ResultsRoutes {
    const val List = "recommendation_list"
    const val Detail = "recommendation_detail"
    private const val ContentTypeArg = "contentType"
    private const val ItemIdArg = "itemId"
    const val DetailPattern = "$Detail/{$ContentTypeArg}/{$ItemIdArg}"

    fun detailRoute(item: RecommendationItem): String {
        return "$Detail/${item.contentType.name}/${Uri.encode(item.id)}"
    }
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
                    navController.navigate(ResultsRoutes.detailRoute(item))
                }
            )
        }
        composable(
            route = ResultsRoutes.DetailPattern,
            arguments = listOf(
                navArgument("contentType") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentTypeRaw = backStackEntry.arguments?.getString("contentType")
            val itemId = backStackEntry.arguments?.getString("itemId")?.let(Uri::decode)
            val contentType = contentTypeRaw?.let {
                runCatching { RecommendationContentType.valueOf(it) }.getOrNull()
            }
            val item = if (contentType != null && itemId != null) {
                viewModel.findItem(contentType, itemId)
            } else {
                null
            }

            if (item == null) {
                LaunchedEffect(contentTypeRaw, itemId) {
                    navController.popBackStack(ResultsRoutes.List, inclusive = false)
                }
            } else {
                RecommendationDetailScreen(
                    item = item,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
