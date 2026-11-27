package com.redcrow.dragonsfirstflight.game.config

import korlibs.math.geom.Size

/** Fixed game constants. */
object DragonGameConfig {
    const val GAME_TITLE: String = "Dragon's First Flight"

    /** Virtual resolution in pixels; the window scales it to fit. */
    val VIRTUAL_SIZE: Size =
        Size(
            width = 1280.0,
            height = 720.0,
        )

    const val GAME_AREA_X: Double = 0.0
    const val GAME_AREA_Y: Double = 0.0
    const val GAME_AREA_WIDTH: Double = 1280.0
}
