package com.example.heartratemonitor.presentation.presentation.data.network.dto

data class SesionEmparejamientoRequest(
    val numeroSerie: String,
    val alias: String?,
    val tipoDispositivo: String,
    val codigoModelo: String,
    val codigoDispositivo: String,
    val sistemaOperativo: String,
    val versionAplicacion: String
)

// OJO: "watchSecret" es un supuesto, confirmar nombre real con backend
data class SesionEmparejamientoResponse(
    val idSesion: String,
    val tokenEmparejamiento: String,
    val watchSecret: String,
    val expiraEn: String
)

data class EstadoEmparejamientoResponse(
    val success: Boolean,
    val estado: String, // PENDIENTE | EMPAREJADO | EXPIRADO | CANCELADO
    val idSesion: String? = null,
    val expiraEn: String? = null,
    val idDispositivo: String? = null,
    val codigoDispositivo: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessTokenExpiraEn: String? = null,
    val message: String? = null
)

data class MedicionRequest(
    val frecuenciaCardiacaBpm: Int,
    val saturacionOxigenoSpO2: Int? = null,
    val timestamp: String
)

data class MedicionResponse(
    val success: Boolean,
    val idMedicion: String? = null
)