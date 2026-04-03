package com.example.euphony.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Section header for Home screen sections
 * Spotify-style bold headers
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-0.5).sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}
