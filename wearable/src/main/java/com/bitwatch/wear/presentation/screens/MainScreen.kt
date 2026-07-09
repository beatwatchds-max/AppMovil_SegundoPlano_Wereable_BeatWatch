package com.bitwatch.wear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    bpm: Int,
    lastReadingTime: Long,
    onModeSelected: (Mode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        HeartRateDisplay(bpm = bpm, lastReadingTime = lastReadingTime)
        ModeButtons(onModeSelected = onModeSelected)
    }
}

@Composable
private fun HeartRateDisplay(bpm: Int, lastReadingTime: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = bpm.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "BPM",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        val timeFormat = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        Text(
            text = "Última toma: ${timeFormat.format(Date(lastReadingTime))}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModeButtons(onModeSelected: (Mode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeButton(
            label = "Reposo",
            icon = "\uD83C\uDF19", // placeholder lunar
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
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class Mode {
    REST, ACTIVITY, NORMAL
}