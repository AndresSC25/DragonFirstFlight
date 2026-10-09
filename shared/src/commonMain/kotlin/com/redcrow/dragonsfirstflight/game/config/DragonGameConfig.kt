package com.redcrow.dragonsfirstflight.game.config

import korlibs.math.geom.Size

object DragonGameConfig {
    const val GAME_TITLE: String = "Dragon's First Flight"

    val VIRTUAL_SIZE: Size = Size(
        width = 1280.0,
        height = 720.0
    )

    const val GAME_AREA_X: Double = 0.0
    const val GAME_AREA_Y: Double = 0.0
    const val GAME_AREA_WIDTH: Double = 1280.0

    const val HUD_X: Double = 20.0
    const val HUD_Y: Double = 15.0
    const val HUD_WIDTH: Double = 1240.0
    const val HUD_HEIGHT: Double = 140.0

    const val BUTTON_WIDTH: Double = 280.0
    const val BUTTON_HEIGHT: Double = 70.0
}