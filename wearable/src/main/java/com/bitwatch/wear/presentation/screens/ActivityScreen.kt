package com.bitwatch.wear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

enum class Intensity { LOW, MODERATE, HIGH }

// Paleta compartida con MainScreen
private val OrangeAccent = Color(0xFFFF9800)
private val BlueAccent = Color(0xFF3D9BFF)
private val GreenAccent = Color(0xFF2ECC71)
private val BackgroundCenter = Color(0xFF102040)
private val BackgroundEdge = Color(0xFF05070F)
private val ChipIdleBg = Color(0xFF1A2436)

@Composable
fun ActivityScreen(
    bpm: Int,
    elapsedTimeMillis: Long,
    onStartClick: (Intensity) -> Unit,
    onStopClick: () -> Unit = {}
) {
    var selectedIntensity by remember { mutableStateOf(Intensity.MODERATE) }
    var isRunning by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(elapsedTimeMillis) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startMs = System.currentTimeMillis()
            val initialElapsed = elapsed
            while (isActive) {
                delay(100)
                elapsed = initialElapsed + (System.currentTimeMillis() - startMs)
            }
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(BackgroundCenter, BackgroundEdge)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val timerFontSize = (maxWidth.value * 0.15f).sp
            val iconSize = (maxWidth.value * 0.075f).dp
            val smallIconSize = (maxWidth.value * 0.045f).dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timeline,
                    contentDescription = "Actividad",
                    tint = OrangeAccent,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatElapsed(elapsed),
                    fontSize = timerFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(smallIconSize)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$bpm bpm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrangeAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IntensityRow(
                    selected = selectedIntensity,
                    onIntensitySelected = { intensity ->
                        selectedIntensity = intensity
                        if (!isRunning) {
                            isRunning = true
                            onStartClick(intensity)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StartStopButton(
                    isRunning = isRunning,
                    onClick = {
                        isRunning = !isRunning
                        if (isRunning) {
                            onStartClick(selectedIntensity)
                        } else {
                            onStopClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun IntensityRow(
    selected: Intensity,
    onIntensitySelected: (Intensity) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IntensityChip(
            label = "Baja",
            selected = selected == Intensity.LOW,
            onClick = { onIntensitySelected(Intensity.LOW) }
        )
        IntensityChip(
            label = "Mod.",
            selected = selected == Intensity.MODERATE,
            onClick = { onIntensitySelected(Intensity.MODERATE) }
        )
        IntensityChip(
            label = "Alta",
            selected = selected == Intensity.HIGH,
            onClick = { onIntensitySelected(Intensity.HIGH) }
        )
    }
}

@Composable
private fun IntensityChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .semantics { testTag = "IntensityChip_$label" }
            .clip(shape)
            .background(if (selected) BlueAccent else ChipIdleBg, shape)
            .then(
                if (selected) Modifier.border(1.dp, BlueAccent, shape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFFC5CCD9)
        )
    }
}

@Composable
private fun StartStopButton(
    isRunning: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    val backgroundColor = if (isRunning) Color(0xFFE74C3C) else GreenAccent

    Box(
        modifier = Modifier
            .semantics { testTag = "StartStopButton" }
            .clip(shape)
            .background(backgroundColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isRunning) "DETENER" else "INICIAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}