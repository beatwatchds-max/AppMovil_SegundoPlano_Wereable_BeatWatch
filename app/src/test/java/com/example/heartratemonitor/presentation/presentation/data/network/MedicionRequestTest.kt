package com.example.heartratemonitor.presentation.presentation.data.network

import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicionRequestTest {

    private val gson = Gson()

    @Test
    fun `serializa los nombres acordados con la app movil`() {
        val json = gson.toJson(
            MedicionRequest(
                frecuenciaCardiacaBpm = 85,
                saturacionOxigenoSpO2 = 97,
                timestamp = "2026-08-13T14:25:49.508Z"
            )
        )

        assertTrue(json.contains("\"frecuenciaCardiacaBpm\":85"))
        assertTrue(json.contains("\"saturacionOxigenoSpO2\":97"))
        assertTrue(json.contains("\"timestamp\":\"2026-08-13T14:25:49.508Z\""))
    }

    @Test
    fun `omite SpO2 cuando aun no existe una lectura`() {
        val json = gson.toJson(
            MedicionRequest(
                frecuenciaCardiacaBpm = 85,
                timestamp = "2026-08-13T14:25:49.508Z"
            )
        )

        assertFalse(json.contains("saturacionOxigenoSpO2"))
    }
}
