package com.example.heartratemonitor.presentation.presentation.ui.screens.pairing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Text
import com.example.heartratemonitor.presentation.presentation.theme.*
import com.tuempresa.heartratemonitor.ui.theme.BackgroundDark
import com.tuempresa.heartratemonitor.ui.theme.CardNavy
import com.tuempresa.heartratemonitor.ui.theme.GraySubtle
import com.tuempresa.heartratemonitor.ui.theme.GreenBpm

@Composable
fun PairingScreen(
    onPaired: () -> Unit,
    viewModel: PairingViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is PairingUiState.Paired) {
            onPaired()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clip(CircleShape)
            .background(CardNavy),
        contentAlignment = Alignment.Center
    ) {
        when (val current = state) {
            is PairingUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Generando código...", color = Color.White, fontSize = 11.sp)
                }
            }
            is PairingUiState.QrReady -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = current.qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR de emparejamiento",
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Escanea con la app", color = GraySubtle, fontSize = 10.sp)
                    Text(text = "para conectar", color = GraySubtle, fontSize = 10.sp)
                }
            }
            is PairingUiState.Expired -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Código vencido",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.startPairingFlow() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBpm)
                    ) {
                        Text(text = "GENERAR OTRO", color = Color.Black, fontSize = 11.sp)
                    }
                }
            }
            is PairingUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error de conexión", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = current.message, color = GraySubtle, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.startPairingFlow() }) {
                        Text(text = "REINTENTAR", fontSize = 11.sp)
                    }
                }
            }
            is PairingUiState.Paired -> { /* navega automáticamente vía LaunchedEffect */ }
        }
    }
}