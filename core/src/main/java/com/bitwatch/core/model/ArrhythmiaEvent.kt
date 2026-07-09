package com.bitwatch.core.model

data class ArrhythmiaEvent(
    val id: String,
    val type: ArrhythmiaType,
    val heartRateData: List<HeartRateData>,
    val detectedAt: Long,
    val severity: SeverityLevel
)

enum class ArrhythmiaType {
    ATRIAL_FIBRILLATION,
    VENTRICULAR_TACHYCARDIA,
    BRADYCARDIA,
    TACHYCARDIA,
    PREMATURE_CONTRACTION,
    UNKNOWN
}

enum class SeverityLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}