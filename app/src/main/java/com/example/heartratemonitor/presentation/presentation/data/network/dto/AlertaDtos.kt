package com.example.heartratemonitor.presentation.presentation.data.network.dto

data class AlertaRequest(
    val tipo: String,
    val valorDetectado: Number,
    val mensaje: String,
    val timestamp: String
)

data class AlertaResponse(
    val success: Boolean,
    val idAlerta: String? = null
)