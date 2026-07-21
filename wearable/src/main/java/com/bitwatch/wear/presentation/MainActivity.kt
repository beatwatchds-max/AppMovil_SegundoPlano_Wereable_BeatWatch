package com.bitwatch.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import com.bitwatch.wear.presentation.navigation.BitWatchNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isPermissionGranted = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPermissionGranted = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFormat(PixelFormat.RGBX_8888)
        window.setBackgroundDrawable(null)

        isPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

        if (!isPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }

        setContent {
            MaterialTheme {
                BitWatchNavHost()
            }
        }
    }
}
