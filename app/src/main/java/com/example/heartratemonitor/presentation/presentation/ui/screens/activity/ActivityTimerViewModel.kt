package com.example.heartratemonitor.presentation.presentation.ui.screens.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heartratemonitor.presentation.presentation.data.alerts.HealthThresholds
import com.example.heartratemonitor.presentation.presentation.data.alerts.NotificationHelper
import com.example.heartratemonitor.presentation.presentation.data.network.AlertsRepository
import com.example.heartratemonitor.presentation.presentation.data.sensors.HeartRateSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val sensorAvailable: Boolean = true
)

class ActivityTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = HeartRateSensorManager(application)
    private val alertsRepository = AlertsRepository(application)

    private val _uiState = MutableStateFlow(ActivityTimerUiState())
    val uiState: StateFlow<ActivityTimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var alertaTiempoEnviada = false

    init {
        listenToSensor()
    }

    private fun listenToSensor() {
        viewModelScope.launch {
            sensorManager.bpmFlow()
                .catch { _uiState.value = _uiState.value.copy(sensorAvailable = false) }
                .collect { bpm ->
                    _uiState.value = _uiState.value.copy(currentBpm = bpm)
                }
        }
    }

    fun onIntensitySelected(intensity: String) {
        _uiState.value = _uiState.value.copy(selectedIntensity = intensity)
    }

    fun onIniciarPressed() {
        if (_uiState.value.isRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(isRunning = true, elapsedSeconds = 0)
        alertaTiempoEnviada = false
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val nuevosSegundos = _uiState.value.elapsedSeconds + 1
                _uiState.value = _uiState.value.copy(elapsedSeconds = nuevosSegundos)
                checkTimeLimit(nuevosSegundos)
            }
        }
    }

    private fun checkTimeLimit(elapsedSeconds: Int) {
        val limiteMinutos = HealthThresholds.limiteParaIntensidad(_uiState.value.selectedIntensity)
        val elapsedMinutos = elapsedSeconds / 60

        if (elapsedMinutos >= limiteMinutos && !alertaTiempoEnviada) {
            alertaTiempoEnviada = true
            val mensaje = "Actividad ${_uiState.value.selectedIntensity} excedió el límite de $limiteMinutos min"

            NotificationHelper.showAlert(
                context = getApplication(),
                title = "Tiempo de actividad excedido",
                message = mensaje,
                notificationId = 1002
            )

            viewModelScope.launch {
                alertsRepository.enviarAlerta(
                    tipo = "TIEMPO_EXCEDIDO",
                    valor = elapsedMinutos,
                    mensaje = mensaje
                )
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}