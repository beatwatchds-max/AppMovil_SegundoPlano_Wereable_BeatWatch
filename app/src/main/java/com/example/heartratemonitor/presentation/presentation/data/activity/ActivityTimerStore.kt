package com.example.heartratemonitor.presentation.presentation.data.activity

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActivityTimerState(
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val selectedIntensity: String = "Mod.",
    val limitSeconds: Int = 25 * 60,
    val limitReached: Boolean = false
)

object ActivityTimerStore {
    private const val PREFS_NAME = "activity_timer_state"
    private const val KEY_RUNNING = "running"
    private const val KEY_START_ELAPSED = "start_elapsed"
    private const val KEY_INTENSITY = "intensity"
    private const val KEY_LIMIT_SECONDS = "limit_seconds"
    private const val KEY_LIMIT_REACHED = "limit_reached"
    private const val KEY_LAST_ELAPSED = "last_elapsed"

    private val mutableState = MutableStateFlow(ActivityTimerState())
    val state: StateFlow<ActivityTimerState> = mutableState.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val running = prefs.getBoolean(KEY_RUNNING, false)
        val startElapsed = prefs.getLong(KEY_START_ELAPSED, 0L)
        val limitSeconds = prefs.getInt(KEY_LIMIT_SECONDS, 25 * 60)
        val storedElapsed = prefs.getInt(KEY_LAST_ELAPSED, 0)
        val now = SystemClock.elapsedRealtime()

        val elapsed = if (running && startElapsed in 1..now) {
            ((now - startElapsed) / 1000L).toInt().coerceAtMost(limitSeconds)
        } else {
            storedElapsed.coerceAtMost(limitSeconds)
        }

        mutableState.value = ActivityTimerState(
            elapsedSeconds = elapsed,
            isRunning = running && startElapsed in 1..now,
            selectedIntensity = prefs.getString(KEY_INTENSITY, "Mod.") ?: "Mod.",
            limitSeconds = limitSeconds,
            limitReached = prefs.getBoolean(KEY_LIMIT_REACHED, false)
        )
    }

    fun start(context: Context, intensity: String, limitSeconds: Int, startElapsed: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_RUNNING, true)
            .putLong(KEY_START_ELAPSED, startElapsed)
            .putString(KEY_INTENSITY, intensity)
            .putInt(KEY_LIMIT_SECONDS, limitSeconds)
            .putBoolean(KEY_LIMIT_REACHED, false)
            .putInt(KEY_LAST_ELAPSED, 0)
            .apply()

        mutableState.value = ActivityTimerState(
            isRunning = true,
            selectedIntensity = intensity,
            limitSeconds = limitSeconds
        )
    }

    fun update(elapsedSeconds: Int) {
        val current = mutableState.value
        val bounded = elapsedSeconds.coerceIn(0, current.limitSeconds)
        mutableState.value = current.copy(elapsedSeconds = bounded)
    }

    fun finish(context: Context, limitReached: Boolean) {
        val current = mutableState.value
        val elapsed = if (limitReached) current.limitSeconds else current.elapsedSeconds

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_RUNNING, false)
            .putBoolean(KEY_LIMIT_REACHED, limitReached)
            .putInt(KEY_LAST_ELAPSED, elapsed)
            .apply()

        mutableState.value = current.copy(
            elapsedSeconds = elapsed,
            isRunning = false,
            limitReached = limitReached
        )
    }
}