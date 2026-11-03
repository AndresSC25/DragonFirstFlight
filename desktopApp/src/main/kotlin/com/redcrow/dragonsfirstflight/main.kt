package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig
import korlibs.render.awt.BaseAwtGameWindow
import java.awt.Dimension

private const val MINIMUM_WINDOW_WIDTH: Int = 1280
private const val MINIMUM_WINDOW_HEIGHT: Int = 720

suspend fun main() {
    launchDragonGame(
        windowCreationConfig =
            GameWindowCreationConfig(
                fullscreen = false,
                resizable = true,
            ),
        onWindowCreated = { gameWindow ->
            val awtWindow = gameWindow as? BaseAwtGameWindow
            awtWindow?.window?.minimumSize =
                Dimension(
                    MINIMUM_WINDOW_WIDTH,
                    MINIMUM_WINDOW_HEIGHT,
                )
        },
    )
}
