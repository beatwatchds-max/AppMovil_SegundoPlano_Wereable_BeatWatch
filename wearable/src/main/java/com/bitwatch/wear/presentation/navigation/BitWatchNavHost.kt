package com.bitwatch.wear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.bitwatch.wear.presentation.screens.ActivityScreen
import com.bitwatch.wear.presentation.screens.LoginScreen
import com.bitwatch.wear.presentation.screens.MainScreen
import com.bitwatch.wear.presentation.viewmodel.LoginViewModel
import com.bitwatch.wear.presentation.viewmodel.MainViewModel

private const val ROUTE_LOGIN = "login"
private const val ROUTE_MAIN = "main"
private const val ROUTE_ACTIVITY = "activity"

@Composable
fun BitWatchNavHost() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN
    ) {
        composable(ROUTE_LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.pairingState) {
                if (uiState.pairingState == com.bitwatch.wear.presentation.viewmodel.PairingState.CONNECTED) {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                onPairClick = viewModel::startPairing,
                onRetryClick = viewModel::retryPairing
            )
        }

        composable(ROUTE_MAIN) {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LifecycleStartEffect(Unit) {
                viewModel.startSampling()
                onStopOrDispose {
                    viewModel.stopSampling()
                }
            }

            MainScreen(
                bpm = uiState.bpm,
                lastReadingTime = uiState.lastReadingTime,
                onModeSelected = { /* TODO: persistir/reportar modo (Reposo/Normal) */ },
                onNavigateToActivity = {
                    navController.navigate(ROUTE_ACTIVITY)
                }
            )
        }

        composable(ROUTE_ACTIVITY) {
            ActivityScreen(
                bpm = 0,
                elapsedTimeMillis = 0L,
                onStartClick = { intensity ->
                    // TODO: iniciar sesión de actividad con la intensidad elegida
                },
                onStopClick = {
                    // TODO: detener/guardar la sesión
                }
            )
        }
    }
}
