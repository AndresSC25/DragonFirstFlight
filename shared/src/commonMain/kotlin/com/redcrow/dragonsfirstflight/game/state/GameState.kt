package com.redcrow.dragonsfirstflight

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

internal class GameState {
    val posicionDragon = mutableStateOf(
        GameConfig.DRAGON_Y_INICIAL
    )

    val velocidadVertical = mutableFloatStateOf(0f)

    val vidas = mutableIntStateOf(
        GameConfig.VIDAS_INICIALES
    )

    val puntos = mutableIntStateOf(0)

    val partidaActiva: Boolean
        get() = vidas.intValue > 0 &&
                puntos.intValue < GameConfig.PUNTOS_PARA_GANAR
}