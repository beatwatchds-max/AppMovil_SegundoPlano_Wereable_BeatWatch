package com.example.heartratemonitor.presentation.presentation.data.network

import android.content.Context
import com.example.heartratemonitor.presentation.presentation.data.local.TokenManager
import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MedicionesRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.dispositivosApi

    suspend fun enviarMedicion(bpm: Int, spo2: Int? = null): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.failure(IllegalStateException("No hay token guardado"))
        val idDispositivo = tokenManager.getIdDispositivo()
            ?: return Result.failure(IllegalStateException("No hay idDispositivo guardado"))

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        return try {
            val response = api.enviarMedicion(
                idDispositivo = idDispositivo,
                authToken = "Bearer $token",
                body = MedicionRequest(
                    frecuenciaCardiacaBpm = bpm,
                    saturacionOxigenoSpO2 = spo2,
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