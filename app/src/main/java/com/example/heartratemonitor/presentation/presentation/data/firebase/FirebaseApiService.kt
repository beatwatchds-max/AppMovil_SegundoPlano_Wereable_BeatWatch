package com.example.heartratemonitor.presentation.presentation.data.firebase

import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface FirebaseApiService {

    @PUT("beatwatch/galaxy-watch-4-classic/ultimaMedicion.json")
    suspend fun guardarUltimaMedicion(
        @Body medicion: MedicionRequest
    ): Response<MedicionRequest>
}