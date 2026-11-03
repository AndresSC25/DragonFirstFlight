package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig

suspend fun main() {
    launchDragonGame(
        windowSize = DragonGameConfig.VIRTUAL_SIZE,
        windowCreationConfig =
            GameWindowCreationConfig(
                fullscreen = false,
                resizable = false,
            ),
    )
}
