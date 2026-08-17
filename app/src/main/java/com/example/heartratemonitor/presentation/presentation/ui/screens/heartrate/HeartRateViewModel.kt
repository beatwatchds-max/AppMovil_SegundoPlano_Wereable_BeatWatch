package com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heartratemonitor.presentation.presentation.data.alerts.HighBpmAlertController
import com.example.heartratemonitor.presentation.presentation.data.sensors.HeartRateSensorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HeartRateUiState(
    val bpm: Int = 0,
    val lastReadingSecondsAgo: Int = 0,
    val sensorAvailable: Boolean = true,
    val errorMessage: String? = null
)

class HeartRateViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sensorManager =
        HeartRateSensorManager(application)

    private val _uiState =
        MutableStateFlow(HeartRateUiState())

    val uiState: StateFlow<HeartRateUiState> =
        _uiState.asStateFlow()

    private var lastReadingTimestamp =
        System.currentTimeMillis()

    init {
        startListening()
        startSecondsCounter()
    }

    fun startListening() {
        viewModelScope.launch {
            sensorManager.bpmFlow()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        sensorAvailable = false,
                        errorMessage = error.message
                    )
                }
                .collect { bpm ->
                    lastReadingTimestamp =
                        System.currentTimeMillis()

                    _uiState.value = _uiState.value.copy(
                        bpm = bpm,
                        lastReadingSecondsAgo = 0,
                        sensorAvailable = true,
                        errorMessage = null
                    )

                    HighBpmAlertController.processReading(
                        context = getApplication(),
                        bpm = bpm
                    )
                }
        }
    }

    private fun startSecondsCounter() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)

                val seconds = (
                        (
                                System.currentTimeMillis() -
                                        lastReadingTimestamp
                                ) / 1_000
                        ).toInt()

                _uiState.value = _uiState.value.copy(
                    lastReadingSecondsAgo = seconds
                )
            }
        }
    }

    fun retryListening() {
        startListening()
    }
}