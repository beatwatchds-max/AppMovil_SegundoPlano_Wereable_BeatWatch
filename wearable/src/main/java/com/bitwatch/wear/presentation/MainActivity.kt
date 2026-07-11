package com.bitwatch.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material.MaterialTheme
import com.bitwatch.wear.presentation.screens.MainScreen
import com.bitwatch.wear.presentation.screens.Mode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = { mode: Mode ->
                        // TODO: Handle mode selection
                    }
                )
            }
        }
    }
}