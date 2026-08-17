package com.example.heartratemonitor.presentation.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material3.AppScaffold
import com.example.heartratemonitor.presentation.presentation.data.sensors.PassiveMonitoringManager
import com.example.heartratemonitor.presentation.presentation.navigation.AppNavigation
import com.example.heartratemonitor.presentation.presentation.theme.HeartRateMonitorTheme
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    private var hasHeartRatePermission by mutableStateOf(false)

    private val heartRatePermission: String
        get() = if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }

    private val backgroundHealthPermission: String?
        get() = when {
            Build.VERSION.SDK_INT >= 36 ->
                "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                Manifest.permission.BODY_SENSORS_BACKGROUND

            else -> null
        }

    private val heartRatePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasHeartRatePermission = granted

            if (granted) {
                solicitarPermisoSegundoPlano()
            } else {
                Log.e(
                    TAG,
                    "Permiso de frecuencia cardiaca rechazado"
                )
                solicitarPermisoNotificaciones()
            }
        }

    private val backgroundPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                registrarMonitoreoPasivo()
            } else {
                Log.e(
                    TAG,
                    "Permiso de sensores en segundo plano rechazado"
                )
            }
            solicitarPermisoNotificaciones()
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Log.w(
                    TAG,
                    "Permiso de notificaciones rechazado; las alertas visuales no se mostrarán"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        hasHeartRatePermission = tienePermiso(
            heartRatePermission
        )

        if (hasHeartRatePermission) {
            solicitarPermisoSegundoPlano()
        } else {
            heartRatePermissionLauncher.launch(
                heartRatePermission
            )
        }

        setContent {
            WearApp()
        }
    }

    private fun solicitarPermisoSegundoPlano() {
        val permiso = backgroundHealthPermission

        if (permiso == null) {
            registrarMonitoreoPasivo()
            solicitarPermisoNotificaciones()
            return
        }

        if (tienePermiso(permiso)) {
            registrarMonitoreoPasivo()
            solicitarPermisoNotificaciones()
        } else {
            backgroundPermissionLauncher.launch(permiso)
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !tienePermiso(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun registrarMonitoreoPasivo() {
        lifecycleScope.launch {
            PassiveMonitoringManager
                .registrar(applicationContext)
                .onSuccess {
                    Log.i(
                        TAG,
                        "Monitoreo pasivo registrado correctamente"
                    )
                }
                .onFailure { error ->
                    Log.e(
                        TAG,
                        "No se pudo registrar el monitoreo pasivo",
                        error
                    )
                }
        }
    }

    private fun tienePermiso(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "PassiveMonitoring"
    }
}

@Composable
fun WearApp() {
    HeartRateMonitorTheme {
        AppScaffold {
            AppNavigation()
        }
    }
}