package com.example.heartratemonitor.presentation.presentation.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.tuempresa.heartratemonitor.ui.theme.BackgroundDark
import com.tuempresa.heartratemonitor.ui.theme.BlueSelected
import com.tuempresa.heartratemonitor.ui.theme.CardNavy
import com.tuempresa.heartratemonitor.ui.theme.ChipUnselected
import com.tuempresa.heartratemonitor.ui.theme.GreenBpm
import com.tuempresa.heartratemonitor.ui.theme.OrangeAccent

@Composable
fun ActivityTimerScreen(
    viewModel: ActivityTimerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clip(CircleShape)
            .background(CardNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.MonitorHeart,
                contentDescription = "Actividad",
                tint = OrangeAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatTime(state.elapsedSeconds),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    state.limitReached -> "Límite alcanzado"
                    state.isRunning -> "Actividad en curso"
                    else -> "Selecciona intensidad"
                },
                color = if (state.limitReached) Color.Red else Color.White,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (state.sensorAvailable) {
                Text(text = "❤ ${state.currentBpm} bpm", color = OrangeAccent, fontSize = 14.sp)
            } else {
                Text(text = "Sensor no disponible", color = Color.White, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IntensityChip(
                    label = "Baja",
                    selected = state.selectedIntensity == "Baja",
                    onClick = { viewModel.onIntensitySelected("Baja") }
                )
                IntensityChip(
                    label = "Mod.",
                    selected = state.selectedIntensity == "Mod.",
                    onClick = { viewModel.onIntensitySelected("Mod.") }
                )
                IntensityChip(
                    label = "Alta",
                    selected = state.selectedIntensity == "Alta",
                    onClick = { viewModel.onIntensitySelected("Alta") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.onIniciarPressed() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRunning) Color.Red else GreenBpm
                ),
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                Text(
                    text = when {
                        state.isRunning -> "DETENER"
                        state.limitReached -> "REINICIAR"
                        else -> "INICIAR"
                    },
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun IntensityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) BlueSelected else ChipUnselected)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}