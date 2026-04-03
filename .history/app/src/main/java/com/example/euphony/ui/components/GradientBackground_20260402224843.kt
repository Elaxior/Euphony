package com.example.euphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
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

    val gradient = Brush.verticalGradient(
        colors = listOf(
            colorScheme.primary.copy(alpha = 0.28f),
            colorScheme.background,
            colorScheme.secondary.copy(alpha = 0.18f)
        ),
        startY = 0f,
        endY = 1500f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        content()
    }
}
