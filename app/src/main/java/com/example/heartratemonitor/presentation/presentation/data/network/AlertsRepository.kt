package com.example.heartratemonitor.presentation.presentation.data.network

import android.content.Context
import com.example.heartratemonitor.presentation.presentation.data.local.TokenManager
import com.example.heartratemonitor.presentation.presentation.data.network.dto.AlertaRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AlertsRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.dispositivosApi

    suspend fun enviarAlerta(tipo: String, valor: Number, mensaje: String): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.failure(IllegalStateException("Sin token"))
        val idDispositivo = tokenManager.getIdDispositivo()
            ?: return Result.failure(IllegalStateException("Sin idDispositivo"))

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        return try {
            val response = api.enviarAlerta(
                idDispositivo = idDispositivo,
                authToken = "Bearer $token",
                body = AlertaRequest(
                    tipo = tipo,
                    valorDetectado = valor,
                    mensaje = mensaje,
                    timestamp = timestamp
                )
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}