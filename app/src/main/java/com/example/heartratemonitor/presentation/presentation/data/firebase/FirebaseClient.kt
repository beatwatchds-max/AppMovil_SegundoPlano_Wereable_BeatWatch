package com.example.heartratemonitor.presentation.presentation.data.firebase

import com.example.heartratemonitor.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FirebaseClient {

    internal val databaseUrl: String = BuildConfig.FIREBASE_DATABASE_URL.also { url ->
        require(url.startsWith("https://")) {
            "FIREBASE_DATABASE_URL debe utilizar HTTPS"
        }
        require(url.endsWith("/")) {
            "FIREBASE_DATABASE_URL debe terminar con /"
        }
    }

    val api: FirebaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(databaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseApiService::class.java)
    }
}
