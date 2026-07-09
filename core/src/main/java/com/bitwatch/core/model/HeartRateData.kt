package com.bitwatch.core.model

data class HeartRateData(
    val bpm: Int,
    val timestamp: Long,
    val confidence: Float
)