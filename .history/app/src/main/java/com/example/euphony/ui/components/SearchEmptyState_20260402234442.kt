package com.example.euphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchEmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try searching with different keywords\nor check your spelling",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (query.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$query\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
