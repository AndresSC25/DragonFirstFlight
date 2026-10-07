package com.redcrow.dragonsfirstflight

import androidx.compose.ui.unit.dp

internal object GameConfig {
    const val VIDAS_INICIALES = 3
    const val PUNTOS_PARA_GANAR = 5

    val ALTO_AREA = 240.dp

    val DRAGON_X = 24.dp
    val DRAGON_Y_INICIAL = 92.dp
    val DRAGON_TAMANO = 56.dp
    val DRAGON_Y_MAXIMA = ALTO_AREA - DRAGON_TAMANO

    // Gravedad en dp/s² y velocidades en dp/s.
    // El eje vertical aumenta hacia abajo.
    const val GRAVEDAD = 600f
    const val VELOCIDAD_IMPULSO = -240f
    const val VELOCIDAD_MAXIMA_CAIDA = 360f

    val OBSTACULO_Y = 96.dp
    val OBSTACULO_TAMANO = 48.dp
    val MARGEN_INICIAL_OBSTACULO = 72.dp

    // Distancia recorrida por el obstáculo en un segundo.
    val VELOCIDAD_OBSTACULO = 120.dp

    const val MAX_DELTA_SEGUNDOS = 0.05f
}