package com.bitwatch.wear.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme

@Preview(device = "wear_round_small", showSystemUi = true)
@Composable
fun MainScreenRoundPreview() {
    MaterialTheme {
        MainScreen(
            bpm = 72,
            lastReadingTime = System.currentTimeMillis(),
            onModeSelected = {}
        )
    }
}

@Preview(device = "wear_square_small", showSystemUi = true)
@Composable
fun MainScreenSquarePreview() {
    MaterialTheme {
        MainScreen(
            bpm = 72,
            lastReadingTime = System.currentTimeMillis(),
            onModeSelected = {}
        )
    }
}