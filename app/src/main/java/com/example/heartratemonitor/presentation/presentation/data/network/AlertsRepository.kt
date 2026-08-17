package com.example.heartratemonitor.presentation.presentation.data.network

import android.content.Context
import android.util.Log
import com.example.heartratemonitor.presentation.presentation.data.local.TokenManager
import com.example.heartratemonitor.presentation.presentation.data.network.dto.AlertaRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AlertsRepository(context: Context) {

    private val tokenManager = TokenManager(context.applicationContext)
    private val api = RetrofitClient.dispositivosApi

    suspend fun enviarAlerta(
        tipo: String,
        valor: Number,
        mensaje: String
    ): Result<Unit> {

        val token = tokenManager.getAccessToken()

        if (token.isNullOrBlank()) {
            Log.e(TAG, "No se envió la alerta: no existe accessToken")
            return Result.failure(
                IllegalStateException("El reloj no tiene un accessToken")
            )
        }

        val idDispositivo = tokenManager.getIdDispositivo()

        if (idDispositivo.isNullOrBlank()) {
            Log.e(TAG, "No se envió la alerta: no existe idDispositivo")
            return Result.failure(
                IllegalStateException("El reloj no tiene un idDispositivo")
            )
        }

        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val request = AlertaRequest(
            tipo = tipo.trim().uppercase(Locale.US),
            valorDetectado = valor.toDouble(),
            mensaje = mensaje,
            timestamp = timestamp
        )

        Log.i(
            TAG,
            "Enviando alerta: dispositivo=$idDispositivo, " +
                    "tipo=${request.tipo}, valor=${request.valorDetectado}"
        )

        return try {
            val response = api.enviarAlerta(
                idDispositivo = idDispositivo,

                // Se manda el token directamente, sin agregar "Bearer ".
                watchAccessToken = token,

                body = request
            )

            if (response.isSuccessful) {
                val idAlerta = response.body()?.idAlerta

                Log.i(
                    TAG,
                    "Alerta enviada correctamente. " +
                            "HTTP=${response.code()}, idAlerta=$idAlerta"
                )

                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()
                    ?.string()
                    ?.take(500)
                    .orEmpty()

                val mensajeError =
                    "El backend rechazó la alerta. " +
                            "HTTP=${response.code()}, respuesta=$errorBody"

                Log.e(TAG, mensajeError)
                Result.failure(IllegalStateException(mensajeError))
            }
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Error de conexión al enviar la alerta: ${exception.message}",
                exception
            )

            Result.failure(exception)
        }
    }

    private companion object {
        const val TAG = "ALERTA_API"
    }
}