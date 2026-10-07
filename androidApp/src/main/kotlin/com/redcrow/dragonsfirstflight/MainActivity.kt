package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.createDragonGame
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.render.GameWindowCreationConfig
import korlibs.render.KorgwActivity

class MainActivity : KorgwActivity(
    config = GameWindowCreationConfig(
        msaa = 1,
        fullscreen = true
    )
) {
    override suspend fun activityMain() {
        Korge(
            windowSize = DragonGameConfig.VIRTUAL_SIZE,
            virtualSize = DragonGameConfig.VIRTUAL_SIZE,
            backgroundColor = Colors["#101827"]
        ) {
            createDragonGame()
        }
    }
}