package com.example.heartratemonitor.presentation.presentation.data.sensors

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.heartratemonitor.presentation.presentation.data.workers.BpmUploadWorker
import java.util.concurrent.TimeUnit

class PassiveHeartRateService : PassiveListenerService() {

    override fun onNewDataPointsReceived(
        dataPoints: DataPointContainer
    ) {
        val bpm = dataPoints
            .getData(DataType.HEART_RATE_BPM)
            .lastOrNull()
            ?.value
            ?.toInt()
            ?: return

        if (bpm !in 20..250) {
            Log.w(TAG, "Lectura pasiva descartada: $bpm BPM")
            return
        }

        Log.d(TAG, "Lectura pasiva recibida: $bpm BPM")

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val trabajo = OneTimeWorkRequestBuilder<BpmUploadWorker>()
            .setInputData(
                workDataOf(
                    BpmUploadWorker.KEY_BPM to bpm
                )
            )
            .setConstraints(restricciones)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                TRABAJO_ENVIO_BPM,
                ExistingWorkPolicy.REPLACE,
                trabajo
            )
    }

    override fun onPermissionLost() {
        Log.e(
            TAG,
            "Se perdió el permiso para leer BPM en segundo plano"
        )
    }

    companion object {
        private const val TAG = "PassiveHeartRate"
        private const val TRABAJO_ENVIO_BPM =
            "enviar_ultima_bpm_firebase"
    }
}