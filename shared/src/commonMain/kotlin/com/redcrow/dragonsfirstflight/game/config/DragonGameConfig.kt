package com.redcrow.dragonsfirstflight.game.config

import korlibs.math.geom.Size

object DragonGameConfig {
    const val GAME_TITLE: String = "Dragon's First Flight"

    val VIRTUAL_SIZE: Size = Size(
        width = 1280.0,
        height = 720.0
    )

    const val GAME_AREA_X: Double = 50.0
    const val GAME_AREA_Y: Double = 230.0
    const val GAME_AREA_WIDTH: Double = 1180.0

    const val BUTTON_WIDTH: Double = 280.0
    const val BUTTON_HEIGHT: Double = 70.0
}