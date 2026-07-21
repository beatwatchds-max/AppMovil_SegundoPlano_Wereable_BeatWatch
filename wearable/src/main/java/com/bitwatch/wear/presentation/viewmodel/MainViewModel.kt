package com.bitwatch.wear.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitwatch.wear.data.HeartRateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val bpm: Int = 0,
    val lastReadingTime: Long = System.currentTimeMillis(),
    val isSensorAvailable: Boolean = true,
    val isReading: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val heartRateRepository: HeartRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var samplingJob: Job? = null

    @VisibleForTesting
    internal var samplingIntervalMs: Long = SAMPLING_INTERVAL_MS

    companion object {
        internal const val SAMPLING_INTERVAL_MS = 30_000L
    }

    /**
     * Inicia el ciclo de tomas periódicas. Se debe llamar cuando la pantalla
     * principal pasa a primer plano (ON_START) y detener en ON_STOP para no
     * gastar batería con la app en segundo plano.
     */
    fun startSampling() {
        if (samplingJob?.isActive == true) return

        samplingJob = viewModelScope.launch {
            val available = try {
                heartRateRepository.hasHeartRateCapability()
            } catch (e: Exception) {
                false
            }
            _uiState.update { it.copy(isSensorAvailable = available) }

            if (!available) return@launch

            while (isActive) {
                _uiState.update { it.copy(isReading = true) }

                val bpm = try {
                    heartRateRepository.takeSingleReading()
                } catch (e: Exception) {
                    null
                }

                if (bpm != null) {
                    _uiState.update {
                        it.copy(
                            bpm = bpm,
                            lastReadingTime = System.currentTimeMillis(),
                            isReading = false
                        )
                    }
                } else {
                    // No se pudo leer a tiempo (sensor ocupado, mal contacto, etc.)
                    _uiState.update { it.copy(isReading = false) }
                }

                delay(samplingIntervalMs)
            }
        }
    }

    fun stopSampling() {
        samplingJob?.cancel()
        samplingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopSampling()
    }
}
