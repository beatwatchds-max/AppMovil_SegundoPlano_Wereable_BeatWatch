package com.bitwatch.wear.data

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Envuelve el MeasureClient de Health Services para obtener lecturas de
 * ritmo cardíaco desde el sensor físico del reloj.
 *
 * Requiere:
 *  - Dependencia: androidx.health:health-services-client:1.1.0-rc02
 *  - Permiso BODY_SENSORS en el AndroidManifest (declarado + solicitado en runtime)
 *  - Que el dispositivo tenga sensor de pulso (se valida con hasHeartRateCapability)
 */
@Singleton
class HeartRateRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val measureClient by lazy {
        try {
            HealthServices.getClient(context).measureClient
        } catch (e: Exception) {
            null
        }
    }

    /** Verifica que el reloj tenga sensor de ritmo cardíaco antes de intentar medir. */
    suspend fun hasHeartRateCapability(): Boolean {
        val client = measureClient ?: return false
        return try {
            val capabilities = client.getCapabilitiesAsync().await()
            DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Flujo continuo de lecturas de bpm mientras haya un colector suscrito.
     * Se desuscribe automáticamente del sensor cuando el Flow se cancela
     * (por eso conviene usarlo con timeout / take en vez de dejarlo abierto).
     */
    private fun heartRateFlow(): Flow<Int> = callbackFlow {
        val client = measureClient
        if (client == null) {
            close()
            return@callbackFlow
        }

        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Se puede usar para mostrar "sensor no disponible" en la UI si se requiere
            }

            override fun onDataReceived(data: DataPointContainer) {
                val bpm = data.getData(DataType.HEART_RATE_BPM)
                    .lastOrNull()
                    ?.value
                    ?.toInt()
                if (bpm != null) {
                    trySend(bpm)
                }
            }
        }

        client.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            client.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
        }
    }

    /**
     * Toma UNA sola lectura: se suscribe al sensor, espera el primer valor
     * válido y se desuscribe de inmediato. Esto es clave para el patrón de
     * "cada cierto tiempo se hace una toma" en vez de monitoreo continuo,
     * ya que reduce el consumo de batería considerablemente.
     */
    suspend fun takeSingleReading(timeoutMillis: Long = 15_000): Int? =
        withTimeoutOrNull(timeoutMillis) {
            heartRateFlow().first()
        }
}
