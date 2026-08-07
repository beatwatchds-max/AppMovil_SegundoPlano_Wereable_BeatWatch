package com.example.heartratemonitor.presentation.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.example.heartratemonitor.presentation.presentation.navigation.AppNavigation
import com.example.heartratemonitor.presentation.presentation.theme.HeartRateMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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