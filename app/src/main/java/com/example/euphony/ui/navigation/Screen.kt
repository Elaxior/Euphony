package com.example.euphony.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Search : Screen(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )

    data object Library : Screen(
        route = "library",
        title = "Library",
        icon = Icons.Default.LibraryMusic
    )

    data object Player : Screen(
        route = "player",
        title = "Player",
        icon = Icons.Default.PlayCircle
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Player
)
