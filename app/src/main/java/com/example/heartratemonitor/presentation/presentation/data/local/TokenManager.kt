package com.example.heartratemonitor.presentation.presentation.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiraEn: String,
        idDispositivo: String
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("access_token_expira_en", expiraEn)
            .putString("id_dispositivo", idDispositivo)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    fun getIdDispositivo(): String? = prefs.getString("id_dispositivo", null)

    fun isPaired(): Boolean = getAccessToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}