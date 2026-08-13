package com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heartratemonitor.presentation.presentation.data.alerts.HealthThresholds
import com.example.heartratemonitor.presentation.presentation.data.alerts.NotificationHelper
import com.example.heartratemonitor.presentation.presentation.data.network.AlertsRepository
import com.example.heartratemonitor.presentation.presentation.data.network.MedicionesRepository
import com.example.heartratemonitor.presentation.presentation.data.sensors.HeartRateSensorManager
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

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = HeartRateSensorManager(application)
    private val medicionesRepository = MedicionesRepository(application)
    private val alertsRepository = AlertsRepository(application)

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    private var lastReadingTimestamp: Long = System.currentTimeMillis()
    private var notifiedOutOfRange = false

    init {
        startListening()
        startSecondsCounter()
        startPeriodicUpload()
    }

    private fun startPeriodicUpload() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                val bpmActual = _uiState.value.bpm
                if (bpmActual > 0) {
                    medicionesRepository.enviarMedicion(bpm = bpmActual)
                        .onFailure { /* log o reintento silencioso, no interrumpe la UI */ }
                }
            }
        }
    }

    fun startListening() {
        viewModelScope.launch {
            sensorManager.bpmFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        sensorAvailable = false,
                        errorMessage = e.message
                    )
                }
                .collect { bpm ->
                    lastReadingTimestamp = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        bpm = bpm,
                        lastReadingSecondsAgo = 0,
                        sensorAvailable = true,
                        errorMessage = null
                    )
                    checkBpmThreshold(bpm)
                }
        }
    }

    private fun checkBpmThreshold(bpm: Int) {
        val fueraDeRango = bpm > HealthThresholds.BPM_MAX || bpm < HealthThresholds.BPM_MIN

        if (fueraDeRango && !notifiedOutOfRange) {
            notifiedOutOfRange = true
            val mensaje = if (bpm > HealthThresholds.BPM_MAX) {
                "Pulso elevado: $bpm bpm (posible arritmia)"
            } else {
                "Pulso bajo: $bpm bpm (posible arritmia)"
            }

            NotificationHelper.showAlert(
                context = getApplication(),
                title = "Pulso anormal detectado",
                message = mensaje,
                notificationId = 1001
            )

            viewModelScope.launch {
                alertsRepository.enviarAlerta(tipo = "PULSO_ANORMAL", valor = bpm, mensaje = mensaje)
            }
        } else if (!fueraDeRango) {
            notifiedOutOfRange = false
        }
    }

    private fun startSecondsCounter() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val seconds = ((System.currentTimeMillis() - lastReadingTimestamp) / 1000).toInt()
                _uiState.value = _uiState.value.copy(lastReadingSecondsAgo = seconds)
            }
        }
    }

    fun retryListening() {
        startListening()
    }
}