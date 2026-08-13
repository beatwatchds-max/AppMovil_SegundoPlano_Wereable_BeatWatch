package com.example.heartratemonitor.presentation.presentation.data.network

import android.content.Context
import android.util.Log
import com.example.heartratemonitor.presentation.presentation.data.firebase.FirebaseApiService
import com.example.heartratemonitor.presentation.presentation.data.firebase.FirebaseClient
import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MedicionesRepository(
    @Suppress("UNUSED_PARAMETER") context: Context? = null,
    private val firebaseApi: FirebaseApiService = FirebaseClient.api,
    private val nowProvider: () -> Date = { Date() }
) {

    suspend fun enviarMedicion(
        bpm: Int,
        spo2: Int? = null
    ): Result<Unit> {

        if (bpm !in 20..250) {
            return Result.failure(
                IllegalArgumentException("BPM fuera de rango: $bpm")
            )
        }

        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(nowProvider())

        val medicion = MedicionRequest(
            frecuenciaCardiacaBpm = bpm,
            saturacionOxigenoSpO2 = spo2,
            timestamp = timestamp
        )

        Log.d(
            "MedicionesFirebase",
            "Enviando a Firebase: bpm=$bpm, timestamp=$timestamp"
        )

        return try {
            val response = firebaseApi.guardarUltimaMedicion(medicion)

            if (response.isSuccessful) {
                Log.d(
                    "MedicionesFirebase",
                    "Guardado correctamente (${response.code()})"
                )
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string()

                Log.e(
                    "MedicionesFirebase",
                    "Error ${response.code()}: $error"
                )

                Result.failure(
                    Exception("Firebase respondió ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Log.e(
                "MedicionesFirebase",
                "Error de conexión: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }
}
