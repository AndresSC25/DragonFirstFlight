package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig

suspend fun main() {
    launchDragonGame(
        windowCreationConfig =
            GameWindowCreationConfig(
                fullscreen = false,
                resizable = false,
            ),
    )
}
