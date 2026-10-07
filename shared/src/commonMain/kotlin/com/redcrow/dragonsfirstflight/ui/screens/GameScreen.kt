package com.redcrow.dragonsfirstflight

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(onVolverAlMenu: () -> Unit) {
    var numeroPartida by remember {
        mutableIntStateOf(0)
    }

    val estado = remember(numeroPartida) {
        GameState()
    }

    val controlador = remember(estado) {
        GameController(estado)
    }

    val posicionDragon by estado.posicionDragon
    var vidas by estado.vidas
    var puntos by estado.puntos

    val partidaActiva = estado.partidaActiva

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

        Text(
            text = "Vidas: $vidas    " +
                    "Puntos: $puntos/${GameConfig.PUNTOS_PARA_GANAR}"
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(GameConfig.ALTO_AREA)
                .clipToBounds()
                .background(Color(0xFFE3F2FD))
                .pointerInput(controlador) {
                    detectTapGestures(
                        onPress = {
                            controlador.aletear()
                        }
                    )
                }
        ) {
            val posicionInicial =
                maxWidth - GameConfig.MARGEN_INICIAL_OBSTACULO

            var posicionObstaculo by remember(
                maxWidth,
                numeroPartida
            ) {
                mutableStateOf(posicionInicial)
            }

            if (partidaActiva) {
                LaunchedEffect(maxWidth, numeroPartida) {
                    var tiempoAnterior = 0L
                    var impactoRegistrado = false
                    var puntoRegistrado = false

                    while (estado.partidaActiva) {
                        val tiempoActual = withFrameNanos { it }

                        if (tiempoAnterior != 0L) {
                            val segundosTranscurridos =
                                (
                                        (tiempoActual - tiempoAnterior) /
                                                1_000_000_000f
                                        ).coerceAtMost(
                                        GameConfig.MAX_DELTA_SEGUNDOS
                                    )

                            // Actualizar el vuelo antes de comprobar colisiones.
                            controlador.actualizarVuelo(
                                segundosTranscurridos
                            )

                            val siguientePosicion =
                                posicionObstaculo -
                                        (
                                                GameConfig.VELOCIDAD_OBSTACULO *
                                                        segundosTranscurridos
                                                )

                            if (
                                siguientePosicion <=
                                -GameConfig.OBSTACULO_TAMANO
                            ) {
                                posicionObstaculo = posicionInicial
                                impactoRegistrado = false
                                puntoRegistrado = false
                            } else {
                                posicionObstaculo = siguientePosicion

                                if (
                                    !impactoRegistrado &&
                                    !puntoRegistrado &&
                                    hayColision(
                                        dragonX = GameConfig.DRAGON_X,
                                        dragonY =
                                            estado.posicionDragon.value,
                                        dragonTamano =
                                            GameConfig.DRAGON_TAMANO,
                                        obstaculoX = posicionObstaculo,
                                        obstaculoY =
                                            GameConfig.OBSTACULO_Y,
                                        obstaculoTamano =
                                            GameConfig.OBSTACULO_TAMANO
                                    )
                                ) {
                                    vidas -= 1
                                    impactoRegistrado = true
                                }

                                if (
                                    !impactoRegistrado &&
                                    !puntoRegistrado &&
                                    posicionObstaculo +
                                    GameConfig.OBSTACULO_TAMANO <=
                                    GameConfig.DRAGON_X
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

            // Representación provisional del dragón.
            Box(
                modifier = Modifier
                    .offset(
                        x = GameConfig.DRAGON_X,
                        y = posicionDragon
                    )
                    .size(GameConfig.DRAGON_TAMANO)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text("D", color = Color.White)
            }

            // Representación provisional del obstáculo.
            Box(
                modifier = Modifier
                    .offset(
                        x = posicionObstaculo,
                        y = GameConfig.OBSTACULO_Y
                    )
                    .size(GameConfig.OBSTACULO_TAMANO)
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center
            ) {
                Text("X", color = Color.White)
            }
        }

        if (!partidaActiva) {
            Text(
                text = if (vidas == 0) {
                    "Fin de partida"
                } else {
                    "¡Victoria! Esquivaste " +
                            "${GameConfig.PUNTOS_PARA_GANAR} obstáculos"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = { numeroPartida += 1 }
            ) {
                Text("Jugar de nuevo")
            }
        }

        Button(onClick = onVolverAlMenu) {
            Text("Volver al menú")
        }
    }
}