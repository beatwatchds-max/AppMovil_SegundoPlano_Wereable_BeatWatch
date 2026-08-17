package com.example.heartratemonitor.presentation.presentation.data.network.dto

data class AlertaRequest(
    val tipo: String,
    val valorDetectado: Double,
    val mensaje: String,
    val timestamp: String
)

data class AlertaResponse(
    val idAlerta: String? = null,
    val tipo: String? = null,
    val valorDetectado: Double? = null,
    val mensaje: String? = null,
    val timestamp: String? = null
)