package com.example.heartratemonitor.presentation.presentation.data.firebase

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FirebaseClient {

    private const val FIREBASE_URL =
        "https://bpm-g2-default-rtdb.firebaseio.com/"

    val api: FirebaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseApiService::class.java)
    }
}