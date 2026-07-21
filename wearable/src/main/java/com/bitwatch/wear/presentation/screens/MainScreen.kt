package com.bitwatch.wear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

enum class Mode { REST, ACTIVITY, NORMAL }

// Paleta basada en el diseño de referencia
private val OrangeAccent = Color(0xFFFF9800)
private val BlueAccent = Color(0xFF3D9BFF)
private val BackgroundCenter = Color(0xFF102040)
private val BackgroundEdge = Color(0xFF05070F)
private val ButtonIdleBg = Color(0xFF1A2436)
private val TextMuted = Color(0xFF9AA5B8)

@Composable
fun MainScreen(
    bpm: Int,
    lastReadingTime: Long,
    onModeSelected: (Mode) -> Unit,
    onNavigateToActivity: () -> Unit = {}
) {
    // Estado local para reflejar visualmente el modo activo (botón resaltado)
    var selectedMode by remember { mutableStateOf(Mode.NORMAL) }

    Scaffold(
        timeText = { TimeText() }
    ) {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        val bpmFontSize = (screenWidthDp * 0.16f).sp
        val iconSize = (screenWidthDp * 0.07f).dp
        val buttonSize = (screenWidthDp * 0.14f).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(BackgroundCenter, BackgroundEdge)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HeartRateDisplay(
                    bpm = bpm,
                    lastReadingTime = lastReadingTime,
                    bpmFontSize = bpmFontSize,
                    iconSize = iconSize
                )

                Spacer(modifier = Modifier.height(14.dp))

                ModeRow(
                    selectedMode = selectedMode,
                    buttonSize = buttonSize,
                    onModeSelected = { mode ->
                        selectedMode = mode
                        onModeSelected(mode)
                        if (mode == Mode.ACTIVITY) {
                            onNavigateToActivity()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HeartRateDisplay(
    bpm: Int,
    lastReadingTime: Long,
    bpmFontSize: TextUnit,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Frecuencia cardíaca",
            tint = OrangeAccent,
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = bpm.toString(),
            fontSize = bpmFontSize,
            fontWeight = FontWeight.Bold,
            color = OrangeAccent
        )

        Text(
            text = "bpm",
            fontSize = 11.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = elapsedTimeLabel(lastReadingTime),
            fontSize = 9.sp,
            color = TextMuted.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModeRow(
    selectedMode: Mode,
    buttonSize: androidx.compose.ui.unit.Dp,
    onModeSelected: (Mode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        ModeButton(
            icon = Icons.Filled.NightsStay,
            label = "Reposo",
            selected = selectedMode == Mode.REST,
            size = buttonSize,
            onClick = { onModeSelected(Mode.REST) }
        )
        ModeButton(
            icon = Icons.Filled.Bolt,
            label = "Actividad",
            selected = selectedMode == Mode.ACTIVITY,
            size = buttonSize,
            onClick = { onModeSelected(Mode.ACTIVITY) }
        )
        ModeButton(
            icon = Icons.Filled.Person,
            label = "Normal",
            selected = selectedMode == Mode.NORMAL,
            size = buttonSize,
            onClick = { onModeSelected(Mode.NORMAL) }
        )
    }
}

@Composable
private fun ModeButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .then(
                    if (selected) {
                        Modifier.border(2.dp, BlueAccent, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonIdleBg
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BlueAccent else Color.White,
                modifier = Modifier.size(size * 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 8.sp,
            color = if (selected) BlueAccent else TextMuted
        )
    }
}

/**
 * Devuelve una etiqueta de tiempo relativo ("Hace Ns", "Hace Nmin", etc.)
 * en vez de la hora absoluta, tal como en el diseño de referencia.
 */
private fun elapsedTimeLabel(lastReadingTime: Long): String {
    val diffSeconds = (System.currentTimeMillis() - lastReadingTime) / 1000
    return when {
        diffSeconds < 60 -> "Hace ${diffSeconds}s"
        diffSeconds < 3600 -> "Hace ${diffSeconds / 60}min"
        else -> "Hace ${diffSeconds / 3600}h"
    }
}