package com.example.heartratemonitor.presentation.presentation.data.alerts

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val CHANNEL_ID = "alertas_salud"
    private const val CHANNEL_NAME = "Alertas de salud"
    private const val STOP_VIBRATION_REQUEST_CODE = 2001

    const val NOTIFICATION_ID_BPM_ALTO = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Alertas de frecuencia cardiaca y salud"

            /*
             * La vibración se controla manualmente para
             * poder mantenerla y detenerla desde el botón.
             */
            enableVibration(false)
        }

        context.getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAlert(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        createChannel(context)
        vibrateOnce(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    /**
     * Muestra la alerta persistente con el botón
     * para detener la vibración.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showHighBpmNotification(
        context: Context,
        bpm: Int
    ) {
        createChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val message =
            "Pulso elevado: $bpm BPM. " +
                    "La alerta finalizará al bajar de 80 BPM."

        val stopIntent = Intent(
            context,
            StopHighBpmVibrationReceiver::class.java
        ).apply {
            action =
                StopHighBpmVibrationReceiver
                    .ACTION_STOP_HIGH_BPM_VIBRATION
        }

        val stopPendingIntent =
            PendingIntent.getBroadcast(
                context,
                STOP_VIBRATION_REQUEST_CODE,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setContentTitle("Pulso elevado")
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(
                    R.drawable.ic_media_pause,
                    "Detener vibración",
                    stopPendingIntent
                )
                .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_BPM_ALTO,
            notification
        )
    }

    /**
     * Comienza la vibración repetitiva:
     * 600 ms vibrando y 400 ms en pausa.
     */
    fun startHighBpmVibration(context: Context) {
        val vibrator =
            context.getSystemService(Vibrator::class.java)

        if (!vibrator.hasVibrator()) {
            return
        }

        val effect = VibrationEffect.createWaveform(
            longArrayOf(
                0,
                600,
                400
            ),
            0
        )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            vibrator.vibrate(
                effect,
                VibrationAttributes.createForUsage(
                    VibrationAttributes.USAGE_ALARM
                )
            )
        } else {
            vibrator.vibrate(effect)
        }
    }

    /**
     * Detiene la vibración y elimina la notificación.
     */
    fun stopHighBpmAlert(context: Context) {
        context.getSystemService(
            Vibrator::class.java
        ).cancel()

        NotificationManagerCompat.from(context).cancel(
            NOTIFICATION_ID_BPM_ALTO
        )
    }

    private fun vibrateOnce(context: Context) {
        val vibrator =
            context.getSystemService(Vibrator::class.java)

        if (!vibrator.hasVibrator()) {
            return
        }

        val effect = VibrationEffect.createWaveform(
            longArrayOf(
                0,
                400,
                200,
                400,
                200,
                700
            ),
            -1
        )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            vibrator.vibrate(
                effect,
                VibrationAttributes.createForUsage(
                    VibrationAttributes.USAGE_ALARM
                )
            )
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun hasNotificationPermission(
        context: Context
    ): Boolean {
        return Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}