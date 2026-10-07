package com.redcrow.dragonsfirstflight.game

import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Size

object DragonGameConfig {
    const val GAME_TITLE: String = "Dragon's First Flight"

    val VIRTUAL_SIZE: Size = Size(
        width = 1280.0,
        height = 720.0
    )
}

suspend fun Container.createDragonGame() {
    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"]
    )

    val dragon = solidRect(
        width = 96.0,
        height = 64.0,
        color = Colors["#F59E0B"]
    ) {
        x = 180.0
        y = DragonGameConfig.VIRTUAL_SIZE.height / 2.0
    }

    text(
        text = DragonGameConfig.GAME_TITLE,
        textSize = 48.0,
        color = Colors.WHITE
    ) {
        centerOnStage()
        y = 100.0
    }

    text(
        text = "KorGE 6.0.0 - Shared commonMain",
        textSize = 24.0,
        color = Colors["#CBD5E1"]
    ) {
        centerOnStage()
        y = 170.0
    }

    text(
        text = "Desktop and Android integration test",
        textSize = 22.0,
        color = Colors["#94A3B8"]
    ) {
        centerOnStage()
        y = DragonGameConfig.VIRTUAL_SIZE.height - 90.0
    }

    dragon.addUpdater { delta ->
        val elapsedSeconds = delta.inWholeMilliseconds / 1000.0

        x += 120.0 * elapsedSeconds

        if (x > DragonGameConfig.VIRTUAL_SIZE.width) {
            x = -width
        }
    }
}