package com.bitwatch.wear.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(device = "wear_square_small")
@Composable
fun MainScreenPreview() {
    BitWatchWearTheme {
        MainScreen(
            bpm = 72,
            lastReadingTime = System.currentTimeMillis(),
            onModeSelected = {}
        )
    }
}

// Tema temporal para preview
@Composable
private fun BitWatchWearTheme(content: @Composable () -> Unit) {
    content()
}