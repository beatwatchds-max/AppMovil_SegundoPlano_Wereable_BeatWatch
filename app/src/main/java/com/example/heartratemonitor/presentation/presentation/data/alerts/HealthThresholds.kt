package com.example.heartratemonitor.presentation.presentation.data.alerts

object HealthThresholds {
    const val BPM_MIN = 40   // ajustar cuando tengan un valor clínico real
    const val BPM_MAX = 75   // valor de prueba, ajustar después

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