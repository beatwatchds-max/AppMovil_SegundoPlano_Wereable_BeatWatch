package com.bitwatch.backend.service

import com.bitwatch.backend.model.SyncRequest
import com.bitwatch.backend.model.SyncResponse

class SyncService {

    fun processSync(request: SyncRequest): SyncResponse {
        // Validación básica
        require(request.patientId.isNotBlank()) { "patientId is required" }

        // TODO: Persistir en MongoDB cuando configuremos BD
        println("Sync received from patient: ${request.patientId}")
        println("Heart rate samples: ${request.heartRateData.size}")
        println("Arrhythmia events: ${request.events.size}")

        return SyncResponse(
            success = true,
            message = "Data received successfully",
            processedAt = System.currentTimeMillis()
        )
    }

    fun getPatientEvents(patientId: String): List<Any> {
        // TODO: Consultar MongoDB
        return emptyList()
    }
}