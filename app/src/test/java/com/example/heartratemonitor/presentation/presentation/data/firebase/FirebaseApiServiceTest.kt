package com.example.heartratemonitor.presentation.presentation.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.PUT

class FirebaseApiServiceTest {

    @Test
    fun `conserva la ruta publica acordada para la ultima medicion`() {
        val method = FirebaseApiService::class.java.declaredMethods.single {
            it.name == "guardarUltimaMedicion"
        }

        val put = requireNotNull(method.getAnnotation(PUT::class.java)) {
            "La operación de Firebase debe conservar la anotación @PUT"
        }

        assertEquals(
            "beatwatch/galaxy-watch-4-classic/ultimaMedicion.json",
            put.value
        )
    }
}