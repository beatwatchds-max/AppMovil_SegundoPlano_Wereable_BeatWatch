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
import android.util.Log
import com.example.heartratemonitor.BuildConfig

data class HeartRateUiState(
    val bpm: Int = 0,
    val lastReadingSecondsAgo: Int = 0,
    val sensorAvailable: Boolean = true,
    val errorMessage: String? = null
)

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = HeartRateSensorManager(application)
   //rivate val medicionesRepository = MedicionesRepository(application)
    private val alertsRepository = AlertsRepository(application)

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    private var lastReadingTimestamp: Long = System.currentTimeMillis()
    private var alertaPulsoAltoActiva = false

    init {
        startListening()
        startSecondsCounter()
        //startPeriodicUpload()  //Esta corresponde al envio periodico de cada 30seg
    }

   //rivate fun startPeriodicUpload() {
   //   viewModelScope.launch {
   //       while (true) {
   //           kotlinx.coroutines.delay(30_000)
   //           val bpmActual = _uiState.value.bpm
   //           if (bpmActual > 0) {
   //               medicionesRepository.enviarMedicion(bpm = bpmActual)
   //                   .onFailure { /* log o reintento silencioso, no interrumpe la UI */ }
   //           }
   //       }
   //   }
   //

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

        if (
            bpm >= HealthThresholds.BPM_ALERTA_ALTA &&
            !alertaPulsoAltoActiva
        ) {
            alertaPulsoAltoActiva = true

            val mensaje = "Pulso elevado detectado: $bpm bpm"

            NotificationHelper.showAlert(
                context = getApplication(),
                title = "Pulso elevado",
                message = mensaje,
                notificationId = 1001
            )

            viewModelScope.launch {
                alertsRepository.enviarAlerta(
                    tipo = "PULSO_ALTO",
                    valor = bpm,
                    mensaje = mensaje
                ).onSuccess {
                    android.util.Log.i(
                        "ALERTA_BPM",
                        "Alerta enviada correctamente: $bpm BPM"
                    )
                }.onFailure { error ->
                    android.util.Log.e(
                        "ALERTA_BPM",
                        "No se pudo enviar la alerta de $bpm BPM",
                        error
                    )
                }
            }
        }

        // La alerta se habilita nuevamente cuando el pulso baja a 85 BPM.
        if (
            alertaPulsoAltoActiva &&
            bpm <= HealthThresholds.BPM_REARME
        ) {
            alertaPulsoAltoActiva = false

            android.util.Log.d(
                "ALERTA_BPM",
                "Alerta reactivada; el pulso bajó a $bpm BPM"
            )
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