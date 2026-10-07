package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.createDragonGame
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.math.geom.Size

suspend fun main() {
    Korge(
        windowSize = Size(
            width = 1280.0,
            height = 720.0
        ),
        virtualSize = DragonGameConfig.VIRTUAL_SIZE,
        backgroundColor = Colors["#101827"]
    ) {
        createDragonGame()
    }
}