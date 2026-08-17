package com.example.heartratemonitor.presentation.presentation.data.alerts

object HealthThresholds {

    // Genera una alerta cuando el pulso llega a 90 BPM o más.
    const val BPM_ALERTA_ALTA = 90

    // Permite generar una nueva alerta después de bajar a 85 BPM.
    // Esto evita alertas repetidas si oscila entre 89 y 90.
    const val BPM_REARME = 85

    const val LIMITE_BAJA_MINUTOS = 15
    const val LIMITE_MODERADA_MINUTOS = 25
    const val LIMITE_ALTA_MINUTOS = 35

    fun limiteParaIntensidad(intensidad: String): Int = when (intensidad) {
        "Baja" -> LIMITE_BAJA_MINUTOS
        "Mod." -> LIMITE_MODERADA_MINUTOS
        "Alta" -> LIMITE_ALTA_MINUTOS
        else -> LIMITE_MODERADA_MINUTOS
    }
}