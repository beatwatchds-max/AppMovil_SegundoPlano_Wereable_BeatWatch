package com.example.heartratemonitor.presentation.presentation.data.alerts

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthThresholdsTest {

    @Test
    fun `devuelve limite correcto para cada intensidad`() {
        assertEquals(15, HealthThresholds.limiteParaIntensidad("Baja"))
        assertEquals(25, HealthThresholds.limiteParaIntensidad("Mod."))
        assertEquals(35, HealthThresholds.limiteParaIntensidad("Alta"))
    }

    @Test
    fun `usa limite moderado para intensidad desconocida`() {
        assertEquals(25, HealthThresholds.limiteParaIntensidad("Desconocida"))
    }
}
