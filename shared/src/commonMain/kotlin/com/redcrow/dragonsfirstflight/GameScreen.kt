package com.redcrow.dragonsfirstflight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(onVolverAlMenu: () -> Unit) {
    var numeroPartida by remember { mutableIntStateOf(0) }

    var posicionDragon by remember(numeroPartida) {
        mutableStateOf(92.dp)
    }
    var vidas by remember(numeroPartida) {
        mutableIntStateOf(3)
    }
    var puntos by remember(numeroPartida) {
        mutableIntStateOf(0)
    }

    val partidaActiva = vidas > 0 && puntos < PUNTOS_PARA_GANAR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dragon's First Flight",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Vidas: $vidas    Puntos: $puntos/$PUNTOS_PARA_GANAR")

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clipToBounds()
                .background(Color(0xFFE3F2FD))
        ) {
            val posicionInicial = maxWidth - 72.dp

            var posicionObstaculo by remember(maxWidth, numeroPartida) {
                mutableStateOf(posicionInicial)
            }

            if (partidaActiva) {
                LaunchedEffect(maxWidth, numeroPartida) {
                    var tiempoAnterior = 0L
                    var impactoRegistrado = false
                    var puntoRegistrado = false

                    while (vidas > 0 && puntos < PUNTOS_PARA_GANAR) {
                        val tiempoActual = withFrameNanos { it }

                        if (tiempoAnterior != 0L) {
                            val segundosTranscurridos =
                                ((tiempoActual - tiempoAnterior) /
                                        1_000_000_000f).coerceAtMost(0.05f)

                            val siguientePosicion =
                                posicionObstaculo -
                                        (120.dp * segundosTranscurridos)

                            if (siguientePosicion <= (-48).dp) {
                                posicionObstaculo = posicionInicial
                                impactoRegistrado = false
                                puntoRegistrado = false
                            } else {
                                posicionObstaculo = siguientePosicion

                                if (
                                    !impactoRegistrado &&
                                    !puntoRegistrado &&
                                    hayColision(
                                        dragonX = 24.dp,
                                        dragonY = posicionDragon,
                                        dragonTamano = 56.dp,
                                        obstaculoX = posicionObstaculo,
                                        obstaculoY = 96.dp,
                                        obstaculoTamano = 48.dp
                                    )
                                ) {
                                    vidas -= 1
                                    impactoRegistrado = true
                                }

                                if (
                                    !impactoRegistrado &&
                                    !puntoRegistrado &&
                                    posicionObstaculo + 48.dp <= 24.dp
                                ) {
                                    puntos += 1
                                    puntoRegistrado = true
                                }
                            }
                        }

                        tiempoAnterior = tiempoActual
                    }
                }
            }

            // Representación provisional del dragón
            Box(
                modifier = Modifier
                    .offset(x = 24.dp, y = posicionDragon)
                    .size(56.dp)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text("D", color = Color.White)
            }

            // Representación provisional del obstáculo
            Box(
                modifier = Modifier
                    .offset(x = posicionObstaculo, y = 96.dp)
                    .size(48.dp)
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center
            ) {
                Text("X", color = Color.White)
            }
        }

        if (partidaActiva) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        posicionDragon =
                            (posicionDragon - 24.dp)
                                .coerceIn(0.dp, 184.dp)
                    }
                ) {
                    Text("Subir")
                }

                Button(
                    onClick = {
                        posicionDragon =
                            (posicionDragon + 24.dp)
                                .coerceIn(0.dp, 184.dp)
                    }
                ) {
                    Text("Bajar")
                }
            }
        } else {
            Text(
                text = if (vidas == 0) {
                    "Fin de partida"
                } else {
                    "¡Victoria! Esquivaste $PUNTOS_PARA_GANAR obstáculos"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = { numeroPartida += 1 }) {
                Text("Jugar de nuevo")
            }
        }

        Button(onClick = onVolverAlMenu) {
            Text("Volver al menú")
        }
    }
}