package com.bitwatch.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitwatch.wear.data.DataLayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PairingState {
    IDLE,
    SCANNING,
    CONNECTED,
    ERROR
}

data class LoginUiState(
    val pairingState: PairingState = PairingState.IDLE,
    val deviceName: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataLayerManager: DataLayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        observePairingResult()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            dataLayerManager.connectedNode.collect { node ->
                if (node != null) {
                    _uiState.update {
                        it.copy(
                            pairingState = PairingState.CONNECTED,
                            deviceName = node.displayName,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            pairingState = PairingState.IDLE,
                            deviceName = null
                        )
                    }
                }
            }
        }
    }

    private fun observePairingResult() {
        viewModelScope.launch {
            dataLayerManager.pairingChannel.consumeEach { result ->
                when (result) {
                    is DataLayerManager.PairingResult.Pending -> {
                        _uiState.update { it.copy(pairingState = PairingState.SCANNING) }
                    }
                    is DataLayerManager.PairingResult.Success -> {
                        _uiState.update {
                            it.copy(
                                pairingState = PairingState.CONNECTED,
                                deviceName = dataLayerManager.connectedNode.value?.displayName ?: "BitWatch Companion"
                            )
                        }
                    }
                    is DataLayerManager.PairingResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                pairingState = PairingState.ERROR,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun startPairing() {
        _uiState.update { it.copy(pairingState = PairingState.SCANNING, errorMessage = null) }

        val result = dataLayerManager.startPairing()
        when (result) {
            is DataLayerManager.PairingResult.Pending -> {
                // UI already updated to SCANNING, wait for channel callback
            }
            is DataLayerManager.PairingResult.Success -> {
                _uiState.update {
                    it.copy(
                        pairingState = PairingState.CONNECTED,
                        deviceName = dataLayerManager.connectedNode.value?.displayName ?: "BitWatch Companion"
                    )
                }
            }
            is DataLayerManager.PairingResult.Failure -> {
                _uiState.update {
                    it.copy(
                        pairingState = PairingState.ERROR,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun retryPairing() {
        _uiState.update { LoginUiState() }
        startPairing()
    }
}