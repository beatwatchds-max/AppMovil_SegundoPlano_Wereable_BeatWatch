package com.example.heartratemonitor.presentation.presentation.data.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopHighBpmVibrationReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (
            intent.action !=
            ACTION_STOP_HIGH_BPM_VIBRATION
        ) {
            return
        }

        HighBpmAlertController.silenceCurrentAlert(
            context.applicationContext
        )
    }

    companion object {
        const val ACTION_STOP_HIGH_BPM_VIBRATION =
            "com.example.heartratemonitor.STOP_HIGH_BPM_VIBRATION"
    }
}