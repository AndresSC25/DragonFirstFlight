package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.audio.launchAudioPreview
import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig

suspend fun main() {
    if (System.getProperty("dragons.audioPreview") == "true") {
        launchAudioPreview { views -> installDesktopWindowLifecycle(views.gameWindow) }
        return
    }
    launchDragonGame(
        windowSize = DragonGameConfig.VIRTUAL_SIZE,
        windowCreationConfig =
            GameWindowCreationConfig(
                fullscreen = false,
                resizable = false,
            ),
        beforeMenu = { views ->
            installDesktopWindowLifecycle(views.gameWindow)
            playIntroVideo(views)
        },
    )
}
