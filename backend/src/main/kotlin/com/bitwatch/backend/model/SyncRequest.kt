package com.bitwatch.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val patientId: String,
    val deviceId: String,
    val heartRateData: List<HeartRateSample>,
    val events: List<ArrhythmiaSample>,
    val syncedAt: Long
)

@Serializable
data class HeartRateSample(
    val bpm: Int,
    val timestamp: Long,
    val confidence: Float
)

@Serializable
data class ArrhythmiaSample(
    val id: String,
    val type: String,
    val detectedAt: Long,
    val severity: String
)

@Serializable
data class SyncResponse(
    val success: Boolean,
    val message: String,
    val processedAt: Long
)