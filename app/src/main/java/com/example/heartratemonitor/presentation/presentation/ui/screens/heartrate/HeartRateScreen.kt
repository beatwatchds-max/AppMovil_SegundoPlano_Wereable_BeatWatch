package com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.example.heartratemonitor.presentation.presentation.theme.*
import com.tuempresa.heartratemonitor.ui.theme.BackgroundDark
import com.tuempresa.heartratemonitor.ui.theme.BlueSelected
import com.tuempresa.heartratemonitor.ui.theme.CardNavy
import com.tuempresa.heartratemonitor.ui.theme.ChipUnselected
import com.tuempresa.heartratemonitor.ui.theme.GraySubtle
import com.tuempresa.heartratemonitor.ui.theme.GreenBpm

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HeartRateScreen(
    onModeSelected: (String) -> Unit,
    viewModel: HeartRateViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf("Normal") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clip(CircleShape)
            .background(CardNavy),
        contentAlignment = Alignment.Center
    ) {
        // Referencia: el lado más chico de la pantalla (funciona igual en redondo/cuadrado)
        val screenSize = minOf(maxWidth, maxHeight)

        val iconSize = screenSize * 0.14f
        val bpmFontSize = (screenSize.value * 0.21f).sp
        val labelFontSize = (screenSize.value * 0.062f).sp
        val smallFontSize = (screenSize.value * 0.048f).sp
        val modeIconSize = screenSize * 0.18f
        val modeInnerIconSize = screenSize * 0.08f
        val modeLabelFontSize = (screenSize.value * 0.04f).sp
        val spacing = screenSize * 0.035f

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "BPM",
                tint = GreenBpm,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(spacing))

            if (state.sensorAvailable) {
                Text(
                    text = "${state.bpm}",
                    color = GreenBpm,
                    fontSize = bpmFontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "bpm", color = Color.White, fontSize = labelFontSize)
                Spacer(modifier = Modifier.height(spacing * 0.5f))
                Text(
                    text = "Hace ${state.lastReadingSecondsAgo}s",
                    color = GraySubtle,
                    fontSize = smallFontSize
                )
            } else {
                Text(text = "Sensor no disponible", color = Color.White, fontSize = labelFontSize)
                Spacer(modifier = Modifier.height(spacing * 0.5f))
                Button(onClick = { viewModel.retryListening() }) {
                    Text(text = "Reintentar", fontSize = smallFontSize)
                }
            }

            Spacer(modifier = Modifier.height(spacing * 1.3f))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing * 0.8f)) {
                ModeIconButton(
                    icon = Icons.Filled.Bedtime,
                    label = "Reposo",
                    selected = selectedMode == "Reposo",
                    buttonSize = modeIconSize,
                    innerIconSize = modeInnerIconSize,
                    labelFontSize = modeLabelFontSize,
                    onClick = {
                        selectedMode = "Reposo"
                        onModeSelected("Reposo")
                    }
                )
                ModeIconButton(
                    icon = Icons.Filled.Bolt,
                    label = "Actividad",
                    selected = selectedMode == "Actividad",
                    buttonSize = modeIconSize,
                    innerIconSize = modeInnerIconSize,
                    labelFontSize = modeLabelFontSize,
                    onClick = {
                        selectedMode = "Actividad"
                        onModeSelected("Actividad")
                    }
                )
                ModeIconButton(
                    icon = Icons.Filled.Person,
                    label = "Normal",
                    selected = selectedMode == "Normal",
                    buttonSize = modeIconSize,
                    innerIconSize = modeInnerIconSize,
                    labelFontSize = modeLabelFontSize,
                    onClick = {
                        selectedMode = "Normal"
                        onModeSelected("Normal")
                    }
                )
            }
        }
    }
}

@Composable
fun ModeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    buttonSize: Dp,
    innerIconSize: Dp,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(if (selected) BlueSelected.copy(alpha = 0.25f) else ChipUnselected)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BlueSelected else GraySubtle,
                modifier = Modifier.size(innerIconSize)
            )
        }
        Spacer(modifier = Modifier.height(buttonSize * 0.1f))
        Text(text = label, color = if (selected) Color.White else GraySubtle, fontSize = labelFontSize)
    }
}