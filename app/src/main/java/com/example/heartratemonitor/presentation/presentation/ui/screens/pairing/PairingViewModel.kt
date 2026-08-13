package com.example.heartratemonitor.presentation.presentation.ui.screens.pairing

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heartratemonitor.presentation.presentation.data.local.DeviceIdentity
import com.example.heartratemonitor.presentation.presentation.data.local.TokenManager
import com.example.heartratemonitor.presentation.presentation.data.network.RetrofitClient
import com.example.heartratemonitor.presentation.presentation.data.network.dto.SesionEmparejamientoRequest
import com.example.heartratemonitor.presentation.presentation.ui.components.generateQrBitmap
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class PairingUiState {
    object Loading : PairingUiState()
    data class QrReady(val qrBitmap: Bitmap) : PairingUiState()
    object Expired : PairingUiState()
    object Paired : PairingUiState()
    data class Error(val message: String) : PairingUiState()
}

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.dispositivosApi
    private val tokenManager = TokenManager(application)
    private val gson = Gson()

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentSecret: String? = null

    init {
        startPairingFlow()
    }

    fun startPairingFlow() {
        pollingJob?.cancel()
        _uiState.value = PairingUiState.Loading

        viewModelScope.launch {
            try {
                val codigoDispositivo = DeviceIdentity.getCodigoDispositivo(getApplication())

                val request = SesionEmparejamientoRequest(
                    numeroSerie = Build.SERIAL.takeIf { it != "unknown" } ?: Build.ID,
                    alias = "${Build.MANUFACTURER} ${Build.MODEL}",
                    tipoDispositivo = "SMARTWATCH",
                    codigoModelo = Build.MODEL,
                    codigoDispositivo = codigoDispositivo,
                    sistemaOperativo = "WEAR_OS",
                    versionAplicacion = "1.0.0"
                )

                val response = api.crearSesionEmparejamiento(request)
                currentSecret = response.watchSecret

                val qrContent = gson.toJson(
                    mapOf(
                        "idSesion" to response.idSesion,
                        "tokenEmparejamiento" to response.tokenEmparejamiento
                    )
                )
                val bitmap = generateQrBitmap(qrContent)
                _uiState.value = PairingUiState.QrReady(bitmap)

                pollEstado(response.idSesion)
            } catch (e: Exception) {
                _uiState.value = PairingUiState.Error(e.message ?: "Error al iniciar emparejamiento")
            }
        }
    }

    private fun pollEstado(idSesion: String) {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(2500)
                try {
                    val secret = currentSecret ?: return@launch
                    val response = api.consultarEstado(idSesion, secret)

                    when {
                        response.code() == 410 -> {
                            _uiState.value = PairingUiState.Expired
                            return@launch
                        }
                        response.isSuccessful -> {
                            val body = response.body() ?: continue
                            when (body.estado) {
                                "EMPAREJADO" -> {
                                    tokenManager.saveTokens(
                                        accessToken = body.accessToken!!,
                                        refreshToken = body.refreshToken!!,
                                        expiraEn = body.accessTokenExpiraEn ?: "",
                                        idDispositivo = body.idDispositivo!!
                                    )
                                    _uiState.value = PairingUiState.Paired
                                    return@launch
                                }
                                "CANCELADO" -> {
                                    _uiState.value = PairingUiState.Expired
                                    return@launch
                                }
                                // PENDIENTE -> seguimos el loop
                            }
                        }
                    }
                } catch (e: HttpException) {
                    if (e.code() == 410) {
                        _uiState.value = PairingUiState.Expired
                        return@launch
                    }
                }
            }
        }
    }

    fun isAlreadyPaired(): Boolean = tokenManager.isPaired()

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}