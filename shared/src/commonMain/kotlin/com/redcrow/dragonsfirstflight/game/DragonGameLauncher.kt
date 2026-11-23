package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.KorgeDisplayMode
import korlibs.korge.view.Views
import korlibs.math.geom.Anchor
import korlibs.math.geom.ScaleMode
import korlibs.math.geom.Size
import korlibs.render.GameWindowCreationConfig

suspend fun launchDragonGame(
    windowSize: Size = DragonGameConfig.VIRTUAL_SIZE,
    windowCreationConfig: GameWindowCreationConfig = GameWindowCreationConfig(),
    beforeMenu: suspend (Views) -> Unit = {},
) {
    Korge(
        windowSize = windowSize,
        virtualSize = DragonGameConfig.VIRTUAL_SIZE,
        displayMode =
            KorgeDisplayMode(
                scaleMode = ScaleMode.SHOW_ALL,
                scaleAnchor = Anchor.MIDDLE_CENTER,
                clipBorders = false,
            ),
        windowCreationConfig = windowCreationConfig,
        backgroundColor = Colors["#101827"],
    ).start {
        beforeMenu(views)
        createDragonGame(
            views = views,
        )
    }
}
