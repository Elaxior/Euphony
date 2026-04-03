package com.example.euphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.euphony.ui.theme.AccentPurple
import com.example.euphony.ui.theme.DeepBackground
import com.example.euphony.ui.theme.MidnightBackground

/**
 * Premium gradient background for screens
 * Purple to black gradient for visual depth
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MidnightBackground,
            DeepBackground,
            Color(0xFF0A0A19)
        ),
        startY = 0f,
        endY = 1700f
    )

    val accentGlow = Brush.radialGradient(
        colors = listOf(
            AccentPurple.copy(alpha = 0.22f),
            Color.Transparent
        ),
        radius = 900f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .background(accentGlow)
    ) {
        content()
    }
}
