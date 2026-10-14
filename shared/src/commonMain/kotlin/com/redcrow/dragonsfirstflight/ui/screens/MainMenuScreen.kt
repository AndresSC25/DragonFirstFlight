package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.SolidRect
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.sprite
import korlibs.korge.view.text
import korlibs.time.milliseconds


private const val MENU_BACKGROUND_PATH =
    "backgrounds/zone1/fortress_courtyard.png"
private const val MENU_PANEL_PATH =
    "ui/panels/panel_medieval.png"
private const val MENU_BUTTON_PATH =
    "ui/buttons/button_normal.png"
private const val MENU_DRAGON_PATH =
    "sprites/dragon/YoungBrassDragon.png"

private const val MENU_DRAGON_FRAME_WIDTH = 32
private const val MENU_DRAGON_FRAME_HEIGHT = 32
private const val MENU_DRAGON_FRAME_COLUMNS = 6
private const val MENU_DRAGON_FRAME_ROWS = 1
private const val MENU_DRAGON_FRAME_DURATION_MS = 140

private const val MENU_PANEL_WIDTH = 600.0
private const val MENU_PANEL_HEIGHT = 480.0
private const val MENU_CONTENT_WIDTH = 470.0
private const val MENU_CONTENT_HEIGHT = 362.0
private const val MENU_BUTTON_WIDTH = 300.0
private const val MENU_BUTTON_HEIGHT = 76.0
private const val MENU_DRAGON_DISPLAY_SIZE = 108.0

internal suspend fun Container.createMainMenuScreen(
    views: Views,
    onPlay: () -> Unit
): Container {
    val backgroundBitmap =
        resourcesVfs[MENU_BACKGROUND_PATH].readBitmap()
    val panelBitmap =
        resourcesVfs[MENU_PANEL_PATH].readBitmap()
    val buttonBitmap =
        resourcesVfs[MENU_BUTTON_PATH].readBitmap()
    val dragonSpriteMap =
        resourcesVfs[MENU_DRAGON_PATH].readBitmap()

    require(
        dragonSpriteMap.width ==
                MENU_DRAGON_FRAME_WIDTH * MENU_DRAGON_FRAME_COLUMNS
    ) {
        "Invalid menu dragon spritesheet width."
    }

    require(
        dragonSpriteMap.height ==
                MENU_DRAGON_FRAME_HEIGHT * MENU_DRAGON_FRAME_ROWS
    ) {
        "Invalid menu dragon spritesheet height."
    }

    val dragonAnimation = SpriteAnimation(
        spriteMap = dragonSpriteMap,
        spriteWidth = MENU_DRAGON_FRAME_WIDTH,
        spriteHeight = MENU_DRAGON_FRAME_HEIGHT,
        marginTop = 0,
        marginLeft = 0,
        columns = MENU_DRAGON_FRAME_COLUMNS,
        rows = MENU_DRAGON_FRAME_ROWS,
        offsetBetweenColumns = 0,
        offsetBetweenRows = 0
    )

    return container {
        val screenWidth = DragonGameConfig.VIRTUAL_SIZE.width
        val screenHeight = DragonGameConfig.VIRTUAL_SIZE.height

        createResponsiveBackdrop(
            bitmap = backgroundBitmap,
            views = views
        )

        image(texture = backgroundBitmap) {
            x = 0.0
            y = 0.0
            scaleX = screenWidth / backgroundBitmap.width.toDouble()
            scaleY = screenHeight / backgroundBitmap.height.toDouble()
            smoothing = false
        }

        solidRect(
            width = screenWidth,
            height = screenHeight,
            color = Colors.BLACK
        ) {
            x = 0.0
            y = 0.0
            alpha = 0.24
        }

        val panelX = (screenWidth - MENU_PANEL_WIDTH) / 2.0
        val panelY = (screenHeight - MENU_PANEL_HEIGHT) / 2.0

        solidRect(
            width = MENU_CONTENT_WIDTH,
            height = MENU_CONTENT_HEIGHT,
            color = Colors["#101827"]
        ) {
            x = (screenWidth - MENU_CONTENT_WIDTH) / 2.0
            y = 178.0
            alpha = 0.60
        }

        image(texture = panelBitmap) {
            x = panelX
            y = panelY
            scaleX = MENU_PANEL_WIDTH / panelBitmap.width.toDouble()
            scaleY = MENU_PANEL_HEIGHT / panelBitmap.height.toDouble()
            smoothing = false
        }

        val titleText = text(
            text = DragonGameConfig.GAME_TITLE,
            textSize = 46.0,
            color = Colors["#F8D66D"]
        )
        titleText.x = (screenWidth - titleText.width) / 2.0
        titleText.y = 215.0

        val subtitleText = text(
            text = "Take flight. Avoid danger. Escape the fortress.",
            textSize = 20.0,
            color = Colors.WHITE
        )
        subtitleText.x = (screenWidth - subtitleText.width) / 2.0
        subtitleText.y = 286.0

        val dragonScale =
            MENU_DRAGON_DISPLAY_SIZE /
                    MENU_DRAGON_FRAME_WIDTH.toDouble()

        val dragon = sprite(initialAnimation = dragonAnimation) {
            x = (screenWidth - MENU_DRAGON_DISPLAY_SIZE) / 2.0
            y = 315.0
            scaleX = dragonScale
            scaleY = dragonScale
            smoothing = false
        }

        dragon.playAnimationLooped(
            spriteAnimation = dragonAnimation,
            spriteDisplayTime =
                MENU_DRAGON_FRAME_DURATION_MS.milliseconds
        )

        createMedievalImageButton(
            x = (screenWidth - MENU_BUTTON_WIDTH) / 2.0,
            y = 430.0,
            width = MENU_BUTTON_WIDTH,
            height = MENU_BUTTON_HEIGHT,
            texture = buttonBitmap,
            label = "Play",
            textSize = 28.0,
            onClick = onPlay
        )

        val instructionText = text(
            text = "Click or tap during the game to flap",
            textSize = 18.0,
            color = Colors.WHITE
        )
        instructionText.x =
            (screenWidth - instructionText.width) / 2.0
        instructionText.y = 515.0
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
    require(width > 0.0) {
        "Button width must be greater than zero."
    }
    require(height > 0.0) {
        "Button height must be greater than zero."
    }

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
    buttonText.x = x + (width - buttonText.width) / 2.0
    buttonText.y = y + (height - buttonText.height) / 2.0

    button.onClick { onClick() }
    buttonText.onClick { onClick() }

    return button
}
