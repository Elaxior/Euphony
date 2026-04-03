package com.example.euphony.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentPink,
    tertiary = AccentSky,
    background = MidnightBackground,
    surface = DeepBackground,
    surfaceVariant = ElevatedSurface,
    primaryContainer = GlassSurface,
    secondaryContainer = GlassSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = MidnightBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSoft
)

private val AllBlackColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = Color(0xFFD9D9D9),
    tertiary = Color(0xFFBFBFBF),
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF0A0A0A),
    primaryContainer = Color(0xFF121212),
    secondaryContainer = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF303030)
)

@Composable
fun EuphonyTheme(
    allBlackTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (allBlackTheme) AllBlackColorScheme else DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Manage status bar icon color (light icons for dark theme)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

            // FIXED: Suppressed deprecation warnings for Android 15
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()

            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.surface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}