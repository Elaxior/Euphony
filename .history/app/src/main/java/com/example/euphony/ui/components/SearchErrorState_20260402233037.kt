package com.example.euphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchErrorState(
    error: String,
    onRetry: () -> Unit,
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
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFFF4444),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = Color(0xFFB3B3B3),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            )
        ) {
            Text("Retry")
        }
    }
}
