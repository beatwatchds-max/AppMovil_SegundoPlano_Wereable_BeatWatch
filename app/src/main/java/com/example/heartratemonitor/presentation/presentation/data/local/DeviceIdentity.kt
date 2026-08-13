package com.example.heartratemonitor.presentation.presentation.data.local

import android.content.Context
import java.util.UUID

object DeviceIdentity {

    private const val PREFS_NAME = "device_identity_prefs"
    private const val KEY_CODIGO_DISPOSITIVO = "codigo_dispositivo"

    fun getCodigoDispositivo(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var codigo = prefs.getString(KEY_CODIGO_DISPOSITIVO, null)
        if (codigo == null) {
            codigo = "watch-${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString(KEY_CODIGO_DISPOSITIVO, codigo).apply()
        }
        return codigo
    }
}