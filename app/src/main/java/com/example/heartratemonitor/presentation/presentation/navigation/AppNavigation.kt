package com.example.heartratemonitor.presentation.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.heartratemonitor.presentation.presentation.data.local.TokenManager
import com.example.heartratemonitor.presentation.presentation.ui.screens.activity.ActivityTimerScreen
import com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate.HeartRateScreen
import com.example.heartratemonitor.presentation.presentation.ui.screens.pairing.PairingScreen

@Composable
fun AppNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val startDestination = remember {
        if (tokenManager.isPaired()) "heartrate" else "pairing"
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("pairing") {
            PairingScreen(
                onPaired = {
                    navController.navigate("heartrate") {
                        popUpTo("pairing") { inclusive = true }
                    }
                }
            )
        }
        composable("heartrate") {
            HeartRateScreen(
                onModeSelected = { mode ->
                    if (mode == "Actividad") {
                        navController.navigate("activityTimer")
                    }
                }
            )
        }
        composable("activityTimer") {
            ActivityTimerScreen()
        }
    }
}