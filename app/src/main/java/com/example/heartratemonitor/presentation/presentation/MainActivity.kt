package com.example.heartratemonitor.presentation.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.wear.compose.material3.AppScaffold
import com.example.heartratemonitor.presentation.presentation.navigation.AppNavigation
import com.example.heartratemonitor.presentation.presentation.theme.HeartRateMonitorTheme

class MainActivity : ComponentActivity() {

    private var hasHeartRatePermission by mutableStateOf(false)

    // El nombre exacto del permiso cambia según la versión de Android/Wear OS
    private val heartRatePermission: String
        get() = if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasHeartRatePermission = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasHeartRatePermission = ContextCompat.checkSelfPermission(
            this, heartRatePermission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasHeartRatePermission) {
            permissionLauncher.launch(heartRatePermission)
        }

        setContent {
            WearApp()
        }
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