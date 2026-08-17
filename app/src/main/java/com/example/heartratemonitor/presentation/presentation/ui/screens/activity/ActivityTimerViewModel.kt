package com.example.heartratemonitor.presentation.presentation.ui.screens.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heartratemonitor.presentation.presentation.data.activity.ActivityTimerStore
import com.example.heartratemonitor.presentation.presentation.data.activity.ActivityTrackingService
import com.example.heartratemonitor.presentation.presentation.data.sensors.HeartRateSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ActivityTimerUiState(
    val currentBpm: Int = 0,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val selectedIntensity: String = "Mod.",
    val sensorAvailable: Boolean = true,
    val limitReached: Boolean = false
)

class ActivityTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = HeartRateSensorManager(application)
    private val _uiState = MutableStateFlow(ActivityTimerUiState())
    val uiState: StateFlow<ActivityTimerUiState> = _uiState.asStateFlow()

    init {
        ActivityTimerStore.initialize(application)
        if (ActivityTimerStore.state.value.isRunning) {
            ActivityTrackingService.resume(application)
        }
        observeTimer()
        listenToSensor()
    }

    private fun observeTimer() {
        viewModelScope.launch {
            ActivityTimerStore.state.collect { timer ->
                _uiState.value = _uiState.value.copy(
                    elapsedSeconds = timer.elapsedSeconds,
                    isRunning = timer.isRunning,
                    selectedIntensity = timer.selectedIntensity,
                    limitReached = timer.limitReached
                )
            }
        }
    }

    private fun listenToSensor() {
        viewModelScope.launch {
            sensorManager.bpmFlow()
                .catch { _uiState.value = _uiState.value.copy(sensorAvailable = false) }
                .collect { bpm ->
                    _uiState.value = _uiState.value.copy(
                        currentBpm = bpm,
                        sensorAvailable = true
                    )
                }
        }
    }

    fun onIntensitySelected(intensity: String) {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(
            selectedIntensity = intensity,
            elapsedSeconds = 0,
            limitReached = false
        )
    }

    fun onIniciarPressed() {
        val context = getApplication<Application>()
        if (_uiState.value.isRunning) {
            ActivityTrackingService.stop(context)
        } else {
            ActivityTrackingService.start(context, _uiState.value.selectedIntensity)
        }
    }
}