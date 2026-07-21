package com.bitwatch.wear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bitwatch.wear.presentation.viewmodel.LoginUiState
import com.bitwatch.wear.presentation.viewmodel.PairingState

private val BackgroundCenter = Color(0xFF102040)
private val BackgroundEdge = Color(0xFF05070F)
private val BlueAccent = Color(0xFF3D9BFF)
private val GreenAccent = Color(0xFF2ECC71)
private val OrangeAccent = Color(0xFFFF9800)
private val RedAccent = Color(0xFFE74C3C)

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onPairClick: () -> Unit,
    onRetryClick: () -> Unit
) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (uiState.pairingState) {
                    PairingState.IDLE -> {
                        Text(
                            text = "BitWatch",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Vincula tu reloj con el teléfono",
                            fontSize = 10.sp,
                            color = Color(0xFFC5CCD9),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .semantics { testTag = "PairButton" }
                                .clip(RoundedCornerShape(50))
                                .background(GreenAccent)
                                .clickable(onClick = onPairClick)
                                .padding(horizontal = 20.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "VINCULAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    PairingState.SCANNING -> {
                        Text(
                            text = "Buscando dispositivo…",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Asegúrate de tener el Bluetooth\nactivado en ambos dispositivos",
                            fontSize = 8.sp,
                            color = Color(0xFFC5CCD9),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Indicador de carga
                        Text(
                            text = "⏳",
                            fontSize = 18.sp
                        )
                    }

                    PairingState.CONNECTED -> {
                        Text(
                            text = "¡Vinculado!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Conectado a:",
                            fontSize = 9.sp,
                            color = Color(0xFFC5CCD9)
                        )
                        Text(
                            text = uiState.deviceName ?: "Dispositivo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    PairingState.ERROR -> {
                        Text(
                            text = "Error de conexión",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.errorMessage ?: "No se pudo conectar",
                            fontSize = 8.sp,
                            color = Color(0xFFC5CCD9),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .semantics { testTag = "RetryButton" }
                                .clip(RoundedCornerShape(50))
                                .background(OrangeAccent)
                                .clickable(onClick = onRetryClick)
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "REINTENTAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
