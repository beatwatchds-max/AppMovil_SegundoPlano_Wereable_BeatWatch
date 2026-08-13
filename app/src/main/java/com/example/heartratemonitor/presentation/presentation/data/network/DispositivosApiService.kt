package com.example.heartratemonitor.presentation.presentation.data.network

import com.example.heartratemonitor.presentation.presentation.data.network.dto.EstadoEmparejamientoResponse
import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionRequest
import com.example.heartratemonitor.presentation.presentation.data.network.dto.MedicionResponse
import com.example.heartratemonitor.presentation.presentation.data.network.dto.SesionEmparejamientoRequest
import com.example.heartratemonitor.presentation.presentation.data.network.dto.SesionEmparejamientoResponse
import com.example.heartratemonitor.presentation.presentation.data.network.dto.AlertaRequest
import com.example.heartratemonitor.presentation.presentation.data.network.dto.AlertaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface DispositivosApiService {

    @POST("api/Dispositivos/sesion-emparejamiento")
    suspend fun crearSesionEmparejamiento(
        @Body request: SesionEmparejamientoRequest
    ): SesionEmparejamientoResponse

    @GET("api/Dispositivos/emparejamiento/{idSesion}/estado")
    suspend fun consultarEstado(
        @Path("idSesion") idSesion: String,
        @Header("X-Watch-Secret") secret: String
    ): Response<EstadoEmparejamientoResponse> // Response<> para poder leer el 410 manualmente

    @POST("api/Dispositivos/{idDispositivo}/mediciones")
    suspend fun enviarMedicion(
        @Path("idDispositivo") idDispositivo: String,
        @Header("Authorization") authToken: String,
        @Body body: MedicionRequest
    ): Response<MedicionResponse>

    @POST("api/Dispositivos/{idDispositivo}/alertas")
    suspend fun enviarAlerta(
        @Path("idDispositivo") idDispositivo: String,
        @Header("Authorization") authToken: String,
        @Body body: AlertaRequest
    ): Response<AlertaResponse>

}