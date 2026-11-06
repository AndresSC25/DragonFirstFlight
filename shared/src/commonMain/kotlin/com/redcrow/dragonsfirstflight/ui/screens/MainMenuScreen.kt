package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.skins.SkinInventory
import com.redcrow.dragonsfirstflight.ui.components.MEDIEVAL_PANEL_BORDER
import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import com.redcrow.dragonsfirstflight.ui.components.medievalPanel
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.Container
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
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
private const val MENU_DRAGON_FRAME_WIDTH = 32
private const val MENU_DRAGON_FRAME_DURATION_MS = 140

private const val MENU_PANEL_WIDTH = 600.0
private const val MENU_PANEL_HEIGHT = 480.0
private const val MENU_PADDING = 24.0
private const val MENU_BUTTON_GAP = 20.0
private const val MENU_BUTTON_HEIGHT = 64.0
private const val MENU_DRAGON_DISPLAY_SIZE = 96.0

internal suspend fun Container.createMainMenuScreen(
    views: Views,
    skinInventory: SkinInventory,
    skinAnimations: Map<String, SpriteAnimation>,
    onPlay: () -> Unit,
    onInventory: () -> Unit,
): Container {
    val backgroundBitmap =
        resourcesVfs[MENU_BACKGROUND_PATH].readBitmap()
    val panelBitmap =
        resourcesVfs[MENU_PANEL_PATH].readBitmap()
    val buttonBitmap =
        resourcesVfs[MENU_BUTTON_PATH].readBitmap()
    return container {
        val screenWidth = DragonGameConfig.VIRTUAL_SIZE.width
        val screenHeight = DragonGameConfig.VIRTUAL_SIZE.height

        createResponsiveBackdrop(
            bitmap = backgroundBitmap,
            views = views,
        )

        createResponsiveShade(
            views = views,
            color = Colors.BLACK,
            shadeAlpha = 0.24,
        )

        val panelX = (screenWidth - MENU_PANEL_WIDTH) / 2.0
        val panelY = (screenHeight - MENU_PANEL_HEIGHT) / 2.0
        val innerX = panelX + MEDIEVAL_PANEL_BORDER
        val innerY = panelY + MEDIEVAL_PANEL_BORDER
        val innerWidth = MENU_PANEL_WIDTH - 2.0 * MEDIEVAL_PANEL_BORDER
        val innerHeight = MENU_PANEL_HEIGHT - 2.0 * MEDIEVAL_PANEL_BORDER
        val buttonWidth = (innerWidth - 2.0 * MENU_PADDING - MENU_BUTTON_GAP) / 2.0

        solidRect(
            width = innerWidth,
            height = innerHeight,
            color = Colors["#101827"],
        ) {
            x = innerX
            y = innerY
            alpha = 0.60
        }

        medievalPanel(panelBitmap, panelX, panelY, MENU_PANEL_WIDTH, MENU_PANEL_HEIGHT)

        val titleText =
            text(
                text = DragonGameConfig.GAME_TITLE,
                textSize = 46.0,
                color = Colors["#F8D66D"],
            )
        titleText.x = (screenWidth - titleText.width) / 2.0
        titleText.y = innerY + MENU_PADDING

        val subtitleText =
            text(
                text = "Take flight. Avoid danger. Escape the fortress.",
                textSize = 20.0,
                color = Colors.WHITE,
            )
        subtitleText.x = (screenWidth - subtitleText.width) / 2.0
        subtitleText.y = innerY + 78.0

        val dragonScale =
            MENU_DRAGON_DISPLAY_SIZE /
                MENU_DRAGON_FRAME_WIDTH.toDouble()

        var displayedSkin = skinInventory.equipped
        val dragonAnimation = skinAnimations.getValue(displayedSkin.spritePath)

        val dragon =
            sprite(initialAnimation = dragonAnimation) {
                x = (screenWidth - MENU_DRAGON_DISPLAY_SIZE) / 2.0
                y = innerY + 106.0
                scaleX = dragonScale
                scaleY = dragonScale
                smoothing = false
            }

        dragon.playAnimationLooped(
            spriteAnimation = dragonAnimation,
            spriteDisplayTime =
                MENU_DRAGON_FRAME_DURATION_MS.milliseconds,
        )

        addUpdater {
            if (skinInventory.equipped != displayedSkin) {
                displayedSkin = skinInventory.equipped
                dragon.playAnimationLooped(
                    spriteAnimation = skinAnimations.getValue(displayedSkin.spritePath),
                    spriteDisplayTime = MENU_DRAGON_FRAME_DURATION_MS.milliseconds,
                    startFrame = 0,
                )
            }
        }

        createMedievalImageButton(
            x = (screenWidth - MENU_BUTTON_GAP) / 2.0 - buttonWidth,
            y = innerY + 214.0,
            width = buttonWidth,
            height = MENU_BUTTON_HEIGHT,
            texture = buttonBitmap,
            label = "Play",
            textSize = 28.0,
            onClick = onPlay,
        )

        createMedievalImageButton(
            x = (screenWidth + MENU_BUTTON_GAP) / 2.0,
            y = innerY + 214.0,
            width = buttonWidth,
            height = MENU_BUTTON_HEIGHT,
            texture = buttonBitmap,
            label = "Inventory",
            textSize = 28.0,
            onClick = onInventory,
        )

        val instructionText =
            text(
                text = "Click or tap during the game to flap",
                textSize = 18.0,
                color = Colors.WHITE,
            )
        instructionText.x =
            (screenWidth - instructionText.width) / 2.0
        instructionText.y = innerY + innerHeight - MENU_PADDING - instructionText.height
    }
}
