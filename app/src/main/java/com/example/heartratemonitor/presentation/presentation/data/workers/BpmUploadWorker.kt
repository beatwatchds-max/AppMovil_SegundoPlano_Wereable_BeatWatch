package com.example.heartratemonitor.presentation.presentation.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.heartratemonitor.presentation.presentation.data.network.MedicionesRepository

class BpmUploadWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val bpm = inputData.getInt(KEY_BPM, BPM_INVALIDO)

        if (bpm !in 20..250) {
            Log.e(TAG, "BPM inválida recibida por WorkManager: $bpm")
            return Result.failure()
        }

        val resultado = MedicionesRepository(
            applicationContext
        ).enviarMedicion(bpm)

        return resultado.fold(
            onSuccess = {
                Log.i(TAG, "BPM enviada desde segundo plano: $bpm")
                Result.success()
            },
            onFailure = { error ->
                Log.e(
                    TAG,
                    "Error al enviar BPM desde segundo plano",
                    error
                )

                if (runAttemptCount < MAX_REINTENTOS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        )
    }

    companion object {
        const val KEY_BPM = "frecuencia_cardiaca_bpm"

        private const val BPM_INVALIDO = -1
        private const val MAX_REINTENTOS = 5
        private const val TAG = "BpmUploadWorker"
    }
}