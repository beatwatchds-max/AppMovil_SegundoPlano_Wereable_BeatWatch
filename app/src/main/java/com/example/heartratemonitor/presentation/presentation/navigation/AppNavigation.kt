package com.example.heartratemonitor.presentation.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.heartratemonitor.presentation.presentation.ui.screens.activity.ActivityTimerScreen
import com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate.HeartRateScreen

@Composable
fun AppNavigation() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "heartrate"
    ) {
        composable("heartrate") {
            HeartRateScreen(
                bpm = 71,
                lastReadingSecondsAgo = 2,
                onModeSelected = { mode ->
                    if (mode == "Actividad") {
                        navController.navigate("activityTimer")
                    }
                }
            )
        }
        composable("activityTimer") {
            ActivityTimerScreen(
                currentBpm = 100,
                elapsedSeconds = 0,
                onIniciar = { intensidad -> /* luego arrancamos el cronómetro */ }
            )
        }
    }
}