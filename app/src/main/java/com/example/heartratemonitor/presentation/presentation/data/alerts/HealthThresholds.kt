package com.example.heartratemonitor.presentation.presentation.data.alerts

object HealthThresholds {

    // La alerta comienza cuando el pulso llega a 80 BPM o más.
    const val BPM_UMBRAL_ALERTA = 80

    const val LIMITE_BAJA_MINUTOS = 15
    const val LIMITE_MODERADA_MINUTOS = 25
    const val LIMITE_ALTA_MINUTOS = 35

    fun debeIniciarAlertaBpm(bpm: Int): Boolean {
        return bpm >= BPM_UMBRAL_ALERTA
    }

    fun debeFinalizarAlertaBpm(bpm: Int): Boolean {
        return bpm < BPM_UMBRAL_ALERTA
    }

    fun limiteParaIntensidad(intensidad: String): Int = when (intensidad) {
        "Baja" -> LIMITE_BAJA_MINUTOS
        "Mod." -> LIMITE_MODERADA_MINUTOS
        "Alta" -> LIMITE_ALTA_MINUTOS
        else -> LIMITE_MODERADA_MINUTOS
    }
}