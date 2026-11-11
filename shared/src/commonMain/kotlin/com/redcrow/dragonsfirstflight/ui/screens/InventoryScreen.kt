package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.skins.DragonSkin
import com.redcrow.dragonsfirstflight.game.skins.SkinInventory
import com.redcrow.dragonsfirstflight.ui.components.MEDIEVAL_PANEL_BORDER
import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import com.redcrow.dragonsfirstflight.ui.components.medievalPanel
import com.redcrow.dragonsfirstflight.ui.components.skinCard
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
import korlibs.korge.view.text

private const val BACKGROUND_PATH = "backgrounds/zone1/fortress_courtyard.png"
private const val PANEL_PATH = "ui/panels/panel_medieval.png"
private const val BUTTON_PATH = "ui/buttons/button_normal.png"
private const val PANEL_WIDTH = 1000.0
private const val PANEL_HEIGHT = 540.0
private const val PANEL_PADDING = 24.0
private const val CARD_HEIGHT = 220.0
private const val CARD_GAP = 14.0

internal suspend fun Container.createInventoryScreen(
    views: Views,
    skinInventory: SkinInventory,
    skinAnimations: Map<String, SpriteAnimation>,
    onBack: () -> Unit,
): Container {
    val backgroundBitmap = resourcesVfs[BACKGROUND_PATH].readBitmap()
    val panelBitmap = resourcesVfs[PANEL_PATH].readBitmap()
    val buttonBitmap = resourcesVfs[BUTTON_PATH].readBitmap()

    return container {
        visible = false
        val screenWidth = DragonGameConfig.VIRTUAL_SIZE.width
        val screenHeight = DragonGameConfig.VIRTUAL_SIZE.height
        val panelX = (screenWidth - PANEL_WIDTH) / 2.0
        val panelY = (screenHeight - PANEL_HEIGHT) / 2.0
        val innerX = panelX + MEDIEVAL_PANEL_BORDER
        val innerY = panelY + MEDIEVAL_PANEL_BORDER
        val innerWidth = PANEL_WIDTH - 2.0 * MEDIEVAL_PANEL_BORDER
        val innerHeight = PANEL_HEIGHT - 2.0 * MEDIEVAL_PANEL_BORDER

        createResponsiveBackdrop(
            bitmap = backgroundBitmap,
            views = views,
        )
        createResponsiveShade(
            views = views,
            color = Colors.BLACK,
            shadeAlpha = 0.24,
        )

        solidRect(
            width = innerWidth,
            height = innerHeight,
            color = Colors["#101827"],
        ) {
            x = innerX
            y = innerY
            alpha = 0.60
        }
        medievalPanel(panelBitmap, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT)

        val title =
            text(
                text = "Inventory",
                textSize = 46.0,
                color = Colors["#F8D66D"],
            )
        title.x = (screenWidth - title.width) / 2.0
        title.y = innerY + PANEL_PADDING

        val cardWidth =
            minOf(
                160.0,
                (innerWidth - 2.0 * PANEL_PADDING - CARD_GAP * (DragonSkin.entries.size - 1)) /
                    DragonSkin.entries.size,
            )
        val rowWidth = cardWidth * DragonSkin.entries.size + CARD_GAP * (DragonSkin.entries.size - 1)
        val cards =
            DragonSkin.entries.mapIndexed { index, skin ->
                val card =
                    container {
                        x =
                            (screenWidth - rowWidth) / 2.0 +
                            index * (cardWidth + CARD_GAP)
                        y = innerY + 85.0
                    }
                val preview =
                    card.skinCard(
                        skin = skin,
                        animation = skinAnimations.getValue(skin.spritePath),
                        width = cardWidth,
                        height = CARD_HEIGHT,
                    )

                val equipped =
                    card.text(
                        text = "Equipped",
                        textSize = 22.0,
                        color = Colors["#F8D66D"],
                    ) {
                        x = (cardWidth - width) / 2.0
                        y = 155.0 + (60.0 - height) / 2.0
                    }
                val equip =
                    card.container {
                        createMedievalImageButton(
                            x = 10.0,
                            y = 155.0,
                            width = cardWidth - 20.0,
                            height = 60.0,
                            texture = buttonBitmap,
                            label = "Equip",
                            textVerticalOffset = 0.0,
                            onClick = { skinInventory.equip(skin) },
                        )
                    }
                val locked =
                    card.text(
                        text = "Locked",
                        textSize = 22.0,
                        color = Colors["#999999"],
                    ) {
                        x = (cardWidth - width) / 2.0
                        y = 155.0 + (60.0 - height) / 2.0
                    }
                val refresh: (DragonSkin) -> Unit = { currentSkin ->
                    val unlocked = skinInventory.isUnlocked(skin)
                    preview.alpha = if (unlocked) 1.0 else 0.35
                    equipped.visible = unlocked && currentSkin == skin
                    equip.visible = unlocked && currentSkin != skin
                    locked.visible = !unlocked
                }
                refresh
            }

        var renderedSkin = skinInventory.equipped
        var renderedUnlockedCount = skinInventory.unlocked.size
        cards.forEach { it(renderedSkin) }
        addUpdater {
            val equipped = skinInventory.equipped
            val unlockedCount = skinInventory.unlocked.size
            if (equipped != renderedSkin || unlockedCount != renderedUnlockedCount) {
                renderedSkin = equipped
                renderedUnlockedCount = unlockedCount
                cards.forEach { it(equipped) }
            }
        }

        createMedievalImageButton(
            x = (screenWidth - 220.0) / 2.0,
            y = innerY + innerHeight - PANEL_PADDING - 64.0,
            width = 220.0,
            height = 64.0,
            texture = buttonBitmap,
            label = "Back",
            textSize = 28.0,
            onClick = onBack,
        )
    }
}
