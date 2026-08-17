package com.example.heartratemonitor.presentation.presentation.data.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.heartratemonitor.presentation.presentation.MainActivity
import com.example.heartratemonitor.presentation.presentation.data.alerts.HealthThresholds
import com.example.heartratemonitor.presentation.presentation.data.alerts.NotificationHelper
import com.example.heartratemonitor.presentation.presentation.data.network.AlertsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createOngoingChannel()
        NotificationHelper.createChannel(this)
        ActivityTimerStore.initialize(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer(intent.getStringExtra(EXTRA_INTENSITY) ?: "Mod.")
            ACTION_STOP -> stopTimerByUser()
            else -> resumeTimerIfNeeded()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startTimer(intensity: String) {
        val limitMinutes = HealthThresholds.limiteParaIntensidad(intensity)
        val startElapsed = SystemClock.elapsedRealtime()
        ActivityTimerStore.start(this, intensity, limitMinutes * 60, startElapsed)
        startForeground(NOTIFICATION_ID_ONGOING, buildOngoingNotification(intensity, startElapsed))
        launchTimer(startElapsed, intensity, limitMinutes)
    }

    private fun resumeTimerIfNeeded() {
        val state = ActivityTimerStore.state.value
        if (!state.isRunning) {
            stopSelf()
            return
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val startElapsed = prefs.getLong(KEY_START_ELAPSED, 0L)
        if (startElapsed <= 0L || startElapsed > SystemClock.elapsedRealtime()) {
            ActivityTimerStore.finish(this, limitReached = false)
            stopSelf()
            return
        }

        val limitMinutes = state.limitSeconds / 60
        startForeground(
            NOTIFICATION_ID_ONGOING,
            buildOngoingNotification(state.selectedIntensity, startElapsed)
        )
        launchTimer(startElapsed, state.selectedIntensity, limitMinutes)
    }

    private fun launchTimer(startElapsed: Long, intensity: String, limitMinutes: Int) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val limitSeconds = limitMinutes * 60
            while (true) {
                val elapsed = ((SystemClock.elapsedRealtime() - startElapsed) / 1000L)
                    .toInt()
                    .coerceAtLeast(0)

                if (elapsed >= limitSeconds) {
                    onLimitReached(intensity, limitMinutes)
                    break
                }

                ActivityTimerStore.update(elapsed)
                delay(1_000L)
            }
        }
    }

    private suspend fun onLimitReached(intensity: String, limitMinutes: Int) {
        ActivityTimerStore.finish(this, limitReached = true)

        val message = "Actividad $intensity: alcanzaste el límite de $limitMinutes minutos"
        NotificationHelper.showAlert(
            context = this,
            title = "Tiempo límite alcanzado",
            message = message,
            notificationId = NOTIFICATION_ID_LIMIT
        )

        AlertsRepository(this).enviarAlerta(
            tipo = "TIEMPO_EXCEDIDO",
            valor = limitMinutes,
            mensaje = message
        ).onSuccess {
            Log.i(TAG, "Alerta de límite enviada al backend")
        }.onFailure { error ->
            Log.e(TAG, "No se pudo enviar la alerta de límite al backend", error)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopTimerByUser() {
        timerJob?.cancel()
        ActivityTimerStore.finish(this, limitReached = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildOngoingNotification(intensity: String, startElapsed: Long) =
        NotificationCompat.Builder(this, CHANNEL_ID_ONGOING)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Actividad $intensity en curso")
            .setContentText("Cronómetro activo en segundo plano")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis() - (SystemClock.elapsedRealtime() - startElapsed))
            .setContentIntent(openAppPendingIntent())
            .addAction(0, "DETENER", stopPendingIntent())
            .build()

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createOngoingChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_ONGOING,
            "Actividad en curso",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantiene activo el cronómetro de ejercicio"
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.heartratemonitor.action.START_ACTIVITY_TIMER"
        const val ACTION_STOP = "com.example.heartratemonitor.action.STOP_ACTIVITY_TIMER"
        const val EXTRA_INTENSITY = "activity_intensity"

        private const val PREFS_NAME = "activity_timer_state"
        private const val KEY_START_ELAPSED = "start_elapsed"
        private const val CHANNEL_ID_ONGOING = "actividad_en_curso"
        private const val NOTIFICATION_ID_ONGOING = 2001
        private const val NOTIFICATION_ID_LIMIT = 2002
        private const val TAG = "ActivityTimerService"

        fun start(context: Context, intensity: String) {
            val intent = Intent(context, ActivityTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INTENSITY, intensity)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ActivityTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ActivityTrackingService::class.java)
            )
        }
    }
}