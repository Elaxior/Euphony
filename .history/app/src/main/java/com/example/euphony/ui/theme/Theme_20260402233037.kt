package com.example.euphony.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PurpleGlow,
    secondary = PinkGlow,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PinkGlow,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun EuphonyTheme(
    darkTheme: Boolean = true, // Force dark theme for Euphony
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme  // Always use dark theme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Manage status bar icon color (light icons for dark theme)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

            // FIXED: Suppressed deprecation warnings for Android 15
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()

            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}