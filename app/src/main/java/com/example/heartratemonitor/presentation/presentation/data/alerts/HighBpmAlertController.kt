package com.example.heartratemonitor.presentation.presentation.data.alerts

import android.content.Context
import android.util.Log
import com.example.heartratemonitor.presentation.presentation.data.network.AlertsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object HighBpmAlertController {

    private const val TAG = "ALERTA_BPM"

    private const val PREFS_NAME = "high_bpm_alert_state"
    private const val KEY_ALERT_ACTIVE = "alert_active"
    private const val KEY_VIBRATION_MUTED = "vibration_muted"

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /*
     * Indica si el proceso actual ya inició la vibración.
     */
    private var vibrationRunning = false

    /**
     * Procesa cada lectura recibida desde la pantalla
     * o desde el servicio de segundo plano.
     */
    @Synchronized
    fun processReading(context: Context, bpm: Int) {
        if (bpm !in 20..250) {
            Log.w(
                TAG,
                "Lectura ignorada por estar fuera de rango: $bpm BPM"
            )
            return
        }

        val appContext = context.applicationContext

        val preferences = appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val alertActive = preferences.getBoolean(
            KEY_ALERT_ACTIVE,
            false
        )

        var vibrationMuted = preferences.getBoolean(
            KEY_VIBRATION_MUTED,
            false
        )

        when {
            HealthThresholds.debeIniciarAlertaBpm(bpm) -> {
                val newEpisode = !alertActive

                /*
                 * Cuando comienza un episodio nuevo se vuelve a
                 * habilitar la vibración.
                 */
                if (newEpisode) {
                    vibrationMuted = false

                    preferences.edit()
                        .putBoolean(KEY_ALERT_ACTIVE, true)
                        .putBoolean(KEY_VIBRATION_MUTED, false)
                        .apply()
                }

                /*
                 * La vibración solamente comienza si el usuario
                 * no la silenció durante este episodio.
                 */
                if (!vibrationMuted && !vibrationRunning) {
                    NotificationHelper.startHighBpmVibration(
                        appContext
                    )

                    NotificationHelper.showHighBpmNotification(
                        context = appContext,
                        bpm = bpm
                    )

                    vibrationRunning = true
                }

                /*
                 * El backend recibe una alerta solamente cuando
                 * empieza el episodio de pulso elevado.
                 */
                if (newEpisode) {
                    sendAlertToBackend(
                        context = appContext,
                        bpm = bpm
                    )
                }
            }

            HealthThresholds.debeFinalizarAlertaBpm(bpm) -> {
                if (
                    alertActive ||
                    vibrationRunning ||
                    vibrationMuted
                ) {
                    preferences.edit()
                        .putBoolean(KEY_ALERT_ACTIVE, false)
                        .putBoolean(KEY_VIBRATION_MUTED, false)
                        .apply()

                    NotificationHelper.stopHighBpmAlert(
                        appContext
                    )

                    vibrationRunning = false

                    Log.i(
                        TAG,
                        "Alerta finalizada; el pulso bajó a $bpm BPM"
                    )
                }
            }
        }
    }

    /**
     * Se ejecuta cuando el usuario pulsa
     * "Detener vibración" en la notificación.
     */
    @Synchronized
    fun silenceCurrentAlert(context: Context) {
        val appContext = context.applicationContext

        val preferences = appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putBoolean(KEY_VIBRATION_MUTED, true)
            .apply()

        NotificationHelper.stopHighBpmAlert(
            appContext
        )

        vibrationRunning = false

        Log.i(
            TAG,
            "El usuario detuvo la vibración del episodio actual"
        )
    }

    private fun sendAlertToBackend(
        context: Context,
        bpm: Int
    ) {
        val message =
            "Pulso elevado detectado: $bpm BPM"

        scope.launch {
            AlertsRepository(context).enviarAlerta(
                tipo = "PULSO_ALTO",
                valor = bpm,
                mensaje = message
            ).onSuccess {
                Log.i(
                    TAG,
                    "Alerta enviada correctamente: $bpm BPM"
                )
            }.onFailure { error ->
                Log.e(
                    TAG,
                    "No se pudo enviar la alerta de $bpm BPM",
                    error
                )
            }
        }
    }
}