package com.bitwatch.wear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import java.text.SimpleDateFormat
import java.util.*

enum class Mode {
    REST, ACTIVITY, NORMAL
}

@Composable
fun MainScreen(
    bpm: Int,
    lastReadingTime: Long,
    onModeSelected: (Mode) -> Unit
) {
    Scaffold(
        timeText = {
            TimeText()
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HeartRateDisplay(bpm = bpm, lastReadingTime = lastReadingTime)
            }
            item {
                ModeRow(onModeSelected = onModeSelected)
            }
        }
    }
}

@Composable
private fun HeartRateDisplay(bpm: Int, lastReadingTime: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = bpm.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )

        Text(
            text = "BPM",
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        Text(
            text = "Última toma: ${timeFormat.format(Date(lastReadingTime))}",
            fontSize = 10.sp,
            color = MaterialTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModeRow(
    onModeSelected: (Mode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeButton(
            label = "Reposo",
            icon = "\uD83C\uDF19",
            onClick = { onModeSelected(Mode.REST) }
        )
        ModeButton(
            label = "Actividad",
            icon = "\u26A1",
            onClick = { onModeSelected(Mode.ACTIVITY) }
        )
        ModeButton(
            label = "Normal",
            icon = "\uD83D\uDE46",
            onClick = { onModeSelected(Mode.NORMAL) }
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    icon: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}