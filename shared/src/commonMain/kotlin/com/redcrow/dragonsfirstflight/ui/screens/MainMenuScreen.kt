package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

internal fun Container.createMainMenuScreen(
    onPlay: () -> Unit
): Container {
    return container {
        text(
            text = DragonGameConfig.GAME_TITLE,
            textSize = 58.0,
            color = Colors.WHITE
        ) {
            x = 350.0
            y = 180.0
        }

        text(
            text = "A shared Kotlin Multiplatform game powered by KorGE",
            textSize = 24.0,
            color = Colors["#94A3B8"]
        ) {
            x = 315.0
            y = 270.0
        }

        createGameButton(
            x = (
                    DragonGameConfig.VIRTUAL_SIZE.width -
                            DragonGameConfig.BUTTON_WIDTH
                    ) / 2.0,
            y = 380.0,
            label = "Play",
            onClick = onPlay
        )

        text(
            text = "Click or tap to control the dragon",
            textSize = 21.0,
            color = Colors["#64748B"]
        ) {
            x = 460.0
            y = 500.0
        }
    }
}

internal fun Container.createGameButton(
    x: Double,
    y: Double,
    width: Double = DragonGameConfig.BUTTON_WIDTH,
    height: Double = DragonGameConfig.BUTTON_HEIGHT,
    label: String,
    onClick: () -> Unit
): SolidRect {
    val button = solidRect(
        width = width,
        height = height,
        color = Colors["#2563EB"]
    ) {
        this.x = x
        this.y = y
    }

    val buttonText = text(
        text = label,
        textSize = 26.0,
        color = Colors.WHITE
    )

    buttonText.x =
        x + (width - buttonText.width) / 2.0

    buttonText.y =
        y + (height - buttonText.height) / 2.0

    button.onClick {
        onClick()
    }

    buttonText.onClick {
        onClick()
    }

    return button
}
