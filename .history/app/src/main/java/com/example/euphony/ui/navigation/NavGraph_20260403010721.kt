package com.example.euphony.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.euphony.di.AppContainer
import com.example.euphony.ui.screens.home.HomeScreen
import com.example.euphony.ui.screens.library.LibraryScreen
import com.example.euphony.ui.screens.player.PlayerScreen
import com.example.euphony.ui.screens.search.SearchScreen
import com.example.euphony.ui.screens.search.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isAllBlackTheme: Boolean,
    onToggleBlackTheme: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                isAllBlackTheme = isAllBlackTheme,
                onToggleBlackTheme = onToggleBlackTheme
                // ViewModel is now created inside HomeScreen with proper factory
            )
        }

        composable("search") {
            val searchViewModel: SearchViewModel = viewModel(
                factory = AppContainer.provideSearchViewModelFactory()
            )
            SearchScreen(
                viewModel = searchViewModel,
                onSongClick = { song ->
                    coroutineScope.launch {
                        AppContainer.providePlaySongUseCase().invoke(song)
                    }
                    onNavigateToPlayer()
                }
            )
        }

        composable("library") {
            LibraryScreen(
                onNavigateToPlayer = onNavigateToPlayer
            )
        }

        composable("player") {
            PlayerScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
