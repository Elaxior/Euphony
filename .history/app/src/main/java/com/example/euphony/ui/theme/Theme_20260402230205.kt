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
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class EuphonyThemePreset(val displayName: String) {
    CLASSIC("Spotify Night"),
    NORD("Nord"),
    DRACULA("Dracula"),
    CATPPUCCIN("Catppuccin Mocha"),
    GRUVBOX("Gruvbox Dark");

    companion object {
        fun fromIndex(index: Int): EuphonyThemePreset {
            return entries.getOrElse(index) { CLASSIC }
        }
    }
}

private val ClassicColorScheme = darkColorScheme(
    primary = Color(0xFF1ED760),
    secondary = Color(0xFF1DB954),
    tertiary = Color(0xFF63E28F),
    background = Color(0xFF0B0D0C),
    surface = Color(0xFF141917),
    onPrimary = Color(0xFF041007),
    onSecondary = Color(0xFF051108),
    onTertiary = Color(0xFF061109),
    onBackground = Color(0xFFE8F4EC),
    onSurface = Color(0xFFDEE9E2)
)

private val NordColorScheme = darkColorScheme(
    primary = Color(0xFF88C0D0),
    secondary = Color(0xFF81A1C1),
    tertiary = Color(0xFFA3BE8C),
    background = Color(0xFF2E3440),
    surface = Color(0xFF3B4252),
    onPrimary = Color(0xFF1B252F),
    onSecondary = Color(0xFF202A39),
    onTertiary = Color(0xFF1F2A1D),
    onBackground = Color(0xFFECEFF4),
    onSurface = Color(0xFFE5E9F0)
)

private val DraculaColorScheme = darkColorScheme(
    primary = Color(0xFFBD93F9),
    secondary = Color(0xFFFF79C6),
    tertiary = Color(0xFF50FA7B),
    background = Color(0xFF191A21),
    surface = Color(0xFF242632),
    onPrimary = Color(0xFF1A1327),
    onSecondary = Color(0xFF2A0E25),
    onTertiary = Color(0xFF072612),
    onBackground = Color(0xFFF8F8F2),
    onSurface = Color(0xFFF2F2EC)
)

private val CatppuccinColorScheme = darkColorScheme(
    primary = Color(0xFF89B4FA),
    secondary = Color(0xFFF5C2E7),
    tertiary = Color(0xFFA6E3A1),
    background = Color(0xFF11111B),
    surface = Color(0xFF1E1E2E),
    onPrimary = Color(0xFF0B1324),
    onSecondary = Color(0xFF281428),
    onTertiary = Color(0xFF112012),
    onBackground = Color(0xFFCAD3F5),
    onSurface = Color(0xFFBAC2DE)
)

private val GruvboxColorScheme = darkColorScheme(
    primary = Color(0xFFFABD2F),
    secondary = Color(0xFFFB4934),
    tertiary = Color(0xFFB8BB26),
    background = Color(0xFF1D2021),
    surface = Color(0xFF282828),
    onPrimary = Color(0xFF2A2111),
    onSecondary = Color(0xFF2D140F),
    onTertiary = Color(0xFF1E2412),
    onBackground = Color(0xFFFBF1C7),
    onSurface = Color(0xFFEBDAB5)
)

internal fun shapesForPreset(preset: EuphonyThemePreset): Shapes {
    return when (preset) {
        EuphonyThemePreset.CLASSIC -> Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp)
        )

        EuphonyThemePreset.NORD -> Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(18.dp)
        )

        EuphonyThemePreset.DRACULA -> Shapes(
            small = CutCornerShape(6.dp),
            medium = CutCornerShape(12.dp),
            large = CutCornerShape(18.dp)
        )

        EuphonyThemePreset.CATPPUCCIN -> Shapes(
            small = RoundedCornerShape(18.dp),
            medium = RoundedCornerShape(26.dp),
            large = RoundedCornerShape(34.dp)
        )

        EuphonyThemePreset.GRUVBOX -> Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
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
        EuphonyThemePreset.NORD -> NordColorScheme
        EuphonyThemePreset.DRACULA -> DraculaColorScheme
        EuphonyThemePreset.CATPPUCCIN -> CatppuccinColorScheme
        EuphonyThemePreset.GRUVBOX -> GruvboxColorScheme
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