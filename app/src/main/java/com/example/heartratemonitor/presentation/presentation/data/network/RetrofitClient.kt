package com.example.heartratemonitor.presentation.presentation.data.network

import com.example.heartratemonitor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://backend-beatwatch.onrender.com/"

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {

            // Evita mostrar credenciales completas en Logcat.
            redactHeader("Authorization")
            redactHeader("X-Watch-Secret")
            redactHeader("X-Watch-Access-Token")

            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val dispositivosApi: DispositivosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DispositivosApiService::class.java)
    }
}