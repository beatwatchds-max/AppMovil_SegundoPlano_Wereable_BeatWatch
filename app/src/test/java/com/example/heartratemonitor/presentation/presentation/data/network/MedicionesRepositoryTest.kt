package com.example.heartratemonitor.presentation.presentation.data.network

import com.example.heartratemonitor.presentation.presentation.data.firebase.FirebaseApiService
import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.Instant
import java.util.Date

class MedicionesRepositoryTest {

    private val fixedDate = Date.from(Instant.parse("2026-08-13T14:25:49.508Z"))

    @Test
    fun `envia una medicion valida a Firebase`() = runTest {
        val api = FakeFirebaseApi(Response.success(validRequest()))
        val repository = repository(api)

        val result = repository.enviarMedicion(bpm = 85)

        assertTrue(result.isSuccess)
        assertEquals(85, api.lastRequest?.frecuenciaCardiacaBpm)
        assertEquals("2026-08-13T14:25:49.508Z", api.lastRequest?.timestamp)
    }

    @Test
    fun `incluye SpO2 cuando esta disponible`() = runTest {
        val api = FakeFirebaseApi(Response.success(validRequest()))

        val result = repository(api).enviarMedicion(bpm = 85, spo2 = 97)

        assertTrue(result.isSuccess)
        assertEquals(97, api.lastRequest?.saturacionOxigenoSpO2)
    }

    @Test
    fun `rechaza BPM menores al rango permitido sin llamar Firebase`() = runTest {
        val api = FakeFirebaseApi(Response.success(validRequest()))

        val result = repository(api).enviarMedicion(bpm = 19)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals(0, api.callCount)
    }

    @Test
    fun `rechaza BPM mayores al rango permitido sin llamar Firebase`() = runTest {
        val api = FakeFirebaseApi(Response.success(validRequest()))

        val result = repository(api).enviarMedicion(bpm = 251)

        assertTrue(result.isFailure)
        assertEquals(0, api.callCount)
    }

    @Test
    fun `reporta error cuando Firebase responde codigo no exitoso`() = runTest {
        val api = FakeFirebaseApi(
            Response.error(403, "Permission denied".toResponseBody())
        )

        val result = repository(api).enviarMedicion(bpm = 85)

        assertTrue(result.isFailure)
        assertEquals("Firebase respondió 403", result.exceptionOrNull()?.message)
    }

    @Test
    fun `reporta excepcion de red sin cerrar la aplicacion`() = runTest {
        val api = FakeFirebaseApi(exception = IllegalStateException("sin red"))

        val result = repository(api).enviarMedicion(bpm = 85)

        assertTrue(result.isFailure)
        assertEquals("sin red", result.exceptionOrNull()?.message)
    }

    private fun repository(api: FirebaseApiService) = MedicionesRepository(
        firebaseApi = api,
        nowProvider = { fixedDate }
    )

    private fun validRequest() = MedicionRequest(
        frecuenciaCardiacaBpm = 85,
        saturacionOxigenoSpO2 = null,
        timestamp = "2026-08-13T14:25:49.508Z"
    )

    private class FakeFirebaseApi(
        private val response: Response<MedicionRequest>? = null,
        private val exception: Exception? = null
    ) : FirebaseApiService {
        var callCount = 0
        var lastRequest: MedicionRequest? = null

        override suspend fun guardarUltimaMedicion(
            medicion: MedicionRequest
        ): Response<MedicionRequest> {
            callCount++
            lastRequest = medicion
            exception?.let { throw it }
            return requireNotNull(response)
        }
    }
}
