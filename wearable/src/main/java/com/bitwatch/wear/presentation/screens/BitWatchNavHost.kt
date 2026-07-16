package com.bitwatch.wear.presentation.screens

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun BitWatchNavHost() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                bpm = 72,
                lastReadingTime = System.currentTimeMillis(),
                onModeSelected = { /* TODO: handle mode selection */ },
                onNavigateToActivity = {
                    navController.navigate("activity")
                }
            )
        }

        composable("activity") {
            ActivityScreen(
                bpm = 72,
                elapsedTimeMillis = 0L,
                onStartClick = { /* TODO: handle start with intensity */ },
                onStopClick = { /* TODO: handle stop */ }
            )
        }
    }
}