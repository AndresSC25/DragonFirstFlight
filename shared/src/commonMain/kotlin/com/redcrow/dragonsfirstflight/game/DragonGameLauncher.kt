package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.math.geom.Anchor
import korlibs.math.geom.ScaleMode
import korlibs.math.geom.Size

suspend fun launchDragonGame(
    windowSize: Size = DragonGameConfig.VIRTUAL_SIZE
) {
    Korge(
        windowSize = windowSize,
        virtualSize = DragonGameConfig.VIRTUAL_SIZE,
        scaleMode = ScaleMode.SHOW_ALL,
        scaleAnchor = Anchor.MIDDLE_CENTER,
        clipBorders = false,
        backgroundColor = Colors["#101827"]
    ) {
        createDragonGame(
            views = views
        )
    }
}
