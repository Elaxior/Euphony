package com.example.euphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Premium gradient background for screens
 * Purple to black gradient for visual depth
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAllBlack = colorScheme.background == Color.Black

    val gradient = Brush.verticalGradient(
        colors = listOf(
            if (isAllBlack) Color.Black else colorScheme.background,
            if (isAllBlack) Color.Black else colorScheme.surface,
            if (isAllBlack) Color(0xFF030303) else Color(0xFF0A0A19)
        ),
        startY = 0f,
        endY = 1700f
    )

    val accentGlow = if (isAllBlack) {
        Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent
            ),
            radius = 900f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.22f),
                Color.Transparent
            ),
            radius = 900f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .background(accentGlow)
    ) {
        content()
    }
}
