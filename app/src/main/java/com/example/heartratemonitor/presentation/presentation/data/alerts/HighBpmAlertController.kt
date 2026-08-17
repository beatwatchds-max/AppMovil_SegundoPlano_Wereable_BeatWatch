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

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /*
     * Indica si esta instancia del proceso ya inició
     * el patrón repetitivo de vibración.
     */
    private var vibrationRunning = false

    /**
     * Puede ser llamado desde la pantalla o desde el monitoreo pasivo.
     *
     * @Synchronized evita que ambos orígenes generen dos alertas
     * simultáneas para la misma lectura.
     */
    @Synchronized
    fun processReading(context: Context, bpm: Int) {
        if (bpm !in 20..250) {
            Log.w(TAG, "Lectura ignorada por estar fuera de rango: $bpm BPM")
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

        when {
            HealthThresholds.debeIniciarAlertaBpm(bpm) -> {
                /*
                 * Si el proceso fue recreado mientras la alerta seguía activa,
                 * se vuelve a garantizar que la vibración esté ejecutándose.
                 */
                if (!vibrationRunning) {
                    NotificationHelper.startHighBpmVibration(appContext)
                    NotificationHelper.showHighBpmNotification(
                        context = appContext,
                        bpm = bpm
                    )

                    vibrationRunning = true
                }

                /*
                 * El backend solamente recibe una alerta al comenzar
                 * el episodio de BPM elevado.
                 */
                if (!alertActive) {
                    preferences.edit()
                        .putBoolean(KEY_ALERT_ACTIVE, true)
                        .apply()

                    sendAlertToBackend(
                        context = appContext,
                        bpm = bpm
                    )
                }
            }

            HealthThresholds.debeFinalizarAlertaBpm(bpm) -> {
                if (alertActive || vibrationRunning) {
                    preferences.edit()
                        .putBoolean(KEY_ALERT_ACTIVE, false)
                        .apply()

                    NotificationHelper.stopHighBpmAlert(appContext)
                    vibrationRunning = false

                    Log.i(
                        TAG,
                        "Alerta finalizada; el pulso bajó a $bpm BPM"
                    )
                }
            }
        }
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