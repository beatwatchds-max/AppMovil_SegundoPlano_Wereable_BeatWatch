package com.example.heartratemonitor.presentation.presentation.ui.screens.heartrate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.example.heartratemonitor.presentation.presentation.theme.*
import com.tuempresa.heartratemonitor.ui.theme.BackgroundDark
import com.tuempresa.heartratemonitor.ui.theme.BlueSelected
import com.tuempresa.heartratemonitor.ui.theme.CardNavy
import com.tuempresa.heartratemonitor.ui.theme.ChipUnselected
import com.tuempresa.heartratemonitor.ui.theme.GraySubtle
import com.tuempresa.heartratemonitor.ui.theme.GreenBpm

@Composable
fun HeartRateScreen(
    bpm: Int,
    lastReadingSecondsAgo: Int,
    onModeSelected: (String) -> Unit
) {
    var selectedMode by remember { mutableStateOf("Normal") }

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
                imageVector = Icons.Filled.Favorite,
                contentDescription = "BPM",
                tint = GreenBpm,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$bpm",
                color = GreenBpm,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "bpm", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hace ${lastReadingSecondsAgo}s",
                color = GraySubtle,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeIconButton(
                    icon = Icons.Filled.Bedtime,
                    label = "Reposo",
                    selected = selectedMode == "Reposo",
                    onClick = {
                        selectedMode = "Reposo"
                        onModeSelected("Reposo")
                    }
                )
                ModeIconButton(
                    icon = Icons.Filled.Bolt,
                    label = "Actividad",
                    selected = selectedMode == "Actividad",
                    onClick = {
                        selectedMode = "Actividad"
                        onModeSelected("Actividad")
                    }
                )
                ModeIconButton(
                    icon = Icons.Filled.Person,
                    label = "Normal",
                    selected = selectedMode == "Normal",
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
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) BlueSelected.copy(alpha = 0.25f) else ChipUnselected)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BlueSelected else GraySubtle,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = if (selected) Color.White else GraySubtle, fontSize = 9.sp)
    }
}