package com.bitwatch.core.network

import com.bitwatch.core.model.ArrhythmiaEvent
import com.bitwatch.core.model.HeartRateData

data class SyncPayload(
    val patientId: String,
    val heartRateData: List<HeartRateData>,
    val events: List<ArrhythmiaEvent>,
    val syncedAt: Long
)