package com.example.heartratemonitor.presentation.presentation.data.sensors

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.setPassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig

object PassiveMonitoringManager {

    suspend fun registrar(context: Context): Result<Unit> {
        return runCatching {
            val client = HealthServices
                .getClient(context.applicationContext)
                .passiveMonitoringClient

            val capacidades = client.getCapabilities()

            require(
                DataType.HEART_RATE_BPM in
                        capacidades.supportedDataTypesPassiveMonitoring
            ) {
                "El dispositivo no soporta frecuencia cardiaca pasiva"
            }

            val configuracion = PassiveListenerConfig.builder()
                .setDataTypes(
                    setOf(DataType.HEART_RATE_BPM)
                )
                .build()

            client.setPassiveListenerService(
                PassiveHeartRateService::class.java,
                configuracion
            )
        }
    }

    suspend fun cancelar(context: Context): Result<Unit> {
        return runCatching {
            HealthServices
                .getClient(context.applicationContext)
                .passiveMonitoringClient
                .clearPassiveListenerServiceAsync()
                .get()
        }
    }
}