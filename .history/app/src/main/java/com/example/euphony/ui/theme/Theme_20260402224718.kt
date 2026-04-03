package com.example.euphony.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.view.WindowCompat

enum class EuphonyThemePreset(val displayName: String) {
    CLASSIC("Classic"),
    SUNSET("Sunset Pulse"),
    FOREST("Forest Echo"),
    MONO("Mono Grid"),
    SOLAR("Solar Jazz");

    companion object {
        fun fromIndex(index: Int): EuphonyThemePreset {
            return entries.getOrElse(index) { CLASSIC }
        }
    }
}

private val ClassicColorScheme = darkColorScheme(
    primary = PurpleGlow,
    secondary = PinkGlow,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B35),
    secondary = Color(0xFFFFB347),
    tertiary = Color(0xFFFF8A65),
    background = Color(0xFF160B0A),
    surface = Color(0xFF261211),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFF3E8),
    onSurface = Color(0xFFFFE6D9)
)

private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF2ECC71),
    secondary = Color(0xFF1ABC9C),
    tertiary = Color(0xFF58D68D),
    background = Color(0xFF07150E),
    surface = Color(0xFF0E2418),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE9FFF1),
    onSurface = Color(0xFFD8FDE6)
)

private val MonoColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    secondary = Color(0xFF9E9E9E),
    tertiary = Color(0xFFBDBDBD),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFECECEC)
)

private val SolarColorScheme = darkColorScheme(
    primary = Color(0xFFFFC107),
    secondary = Color(0xFF00BCD4),
    tertiary = Color(0xFFFFE082),
    background = Color(0xFF0A1023),
    surface = Color(0xFF151F3B),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFE5EEFF)
)

internal fun shapesForPreset(preset: EuphonyThemePreset): Shapes {
    return when (preset) {
        EuphonyThemePreset.CLASSIC -> Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp)
        )

        EuphonyThemePreset.SUNSET -> Shapes(
            small = CutCornerShape(6.dp),
            medium = CutCornerShape(12.dp),
            large = CutCornerShape(18.dp)
        )

        EuphonyThemePreset.FOREST -> Shapes(
            small = RoundedCornerShape(18.dp),
            medium = RoundedCornerShape(26.dp),
            large = RoundedCornerShape(34.dp)
        )

        EuphonyThemePreset.MONO -> Shapes(
            small = RoundedCornerShape(2.dp),
            medium = RoundedCornerShape(4.dp),
            large = RoundedCornerShape(8.dp)
        )

        EuphonyThemePreset.SOLAR -> Shapes(
            small = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            medium = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
            large = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
        )
    }
}

@Composable
fun EuphonyTheme(
    themePreset: EuphonyThemePreset = EuphonyThemePreset.CLASSIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreset) {
        EuphonyThemePreset.CLASSIC -> ClassicColorScheme
        EuphonyThemePreset.SUNSET -> SunsetColorScheme
        EuphonyThemePreset.FOREST -> ForestColorScheme
        EuphonyThemePreset.MONO -> MonoColorScheme
        EuphonyThemePreset.SOLAR -> SolarColorScheme
    }
    val typography = typographyForPreset(themePreset)
    val shapes = shapesForPreset(themePreset)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Manage status bar icon color (light icons for dark theme)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

            // FIXED: Suppressed deprecation warnings for Android 15
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.surface.toArgb()

            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}