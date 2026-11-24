package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.audio.GameAudio
import com.redcrow.dragonsfirstflight.game.audio.GameSound
import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import com.redcrow.dragonsfirstflight.ui.components.MEDIEVAL_PANEL_BORDER
import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import com.redcrow.dragonsfirstflight.ui.components.medievalPanel
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import kotlin.math.min

private const val PANEL_WIDTH = 1100.0
private const val PANEL_HEIGHT = 660.0
private const val PADDING = 24.0
private const val CARD_GAP = 16.0
private const val CARD_HEIGHT = 148.0

private data class MapPreview(
    val zone: GameZone,
    val name: String,
    val path: String,
)

private val maps =
    listOf(
        MapPreview(GameZone.ZONE_ONE, "Fortress Courtyard", "backgrounds/zone1/fortress_courtyard.png"),
        MapPreview(GameZone.ZONE_TWO, "Towers and Walls", "backgrounds/zone2/castle-walls.png"),
        MapPreview(GameZone.ZONE_THREE, "Outer Forest", "backgrounds/zone3/forest.png"),
        MapPreview(GameZone.BOSS, "Final Battlefield", "backgrounds/boss/battlefield.png"),
    )

internal suspend fun Container.createEndlessSetupScreen(
    views: Views,
    audio: GameAudio,
    controller: GameController,
    onStart: () -> Unit,
    onBack: () -> Unit,
): Container {
    val previews = maps.associateWith { resourcesVfs[it.path].readBitmap() }
    val panelBitmap = resourcesVfs["ui/panels/panel_medieval.png"].readBitmap()
    val buttonBitmap = resourcesVfs["ui/buttons/button_normal.png"].readBitmap()

    return container {
        visible = false
        val screenWidth = DragonGameConfig.VIRTUAL_SIZE.width
        val screenHeight = DragonGameConfig.VIRTUAL_SIZE.height
        val panelX = (screenWidth - PANEL_WIDTH) / 2.0
        val panelY = (screenHeight - PANEL_HEIGHT) / 2.0
        val innerX = panelX + MEDIEVAL_PANEL_BORDER
        val innerY = panelY + MEDIEVAL_PANEL_BORDER
        val innerWidth = PANEL_WIDTH - 2.0 * MEDIEVAL_PANEL_BORDER
        val contentWidth = innerWidth - 2.0 * PADDING
        val cardWidth = (contentWidth - CARD_GAP) / 2.0

        createResponsiveBackdrop(previews.getValue(maps.first()), views)
        createResponsiveShade(views, Colors.BLACK, shadeAlpha = 0.45)
        solidRect(innerWidth, PANEL_HEIGHT - 2.0 * MEDIEVAL_PANEL_BORDER, Colors["#101827"]) {
            x = innerX
            y = innerY
        }
        medievalPanel(panelBitmap, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT)

        text("Endless", textSize = 38.0, color = Colors["#F8D66D"]) {
            x = (screenWidth - width) / 2.0
            y = innerY + 8.0
        }
        val difficultyLabel =
            text("", textSize = 22.0, color = Colors.WHITE) {
                y = innerY + 58.0
            }

        lateinit var refreshSelection: () -> Unit
        val refreshCards =
            maps.mapIndexed { index, map ->
                val card =
                    container {
                        x = innerX + PADDING + (index % 2) * (cardWidth + CARD_GAP)
                        y = innerY + 108.0 + (index / 2) * (CARD_HEIGHT + CARD_GAP)
                    }
                val border = card.solidRect(cardWidth, CARD_HEIGHT, Colors["#F8D66D"])
                card.solidRect(cardWidth - 6.0, CARD_HEIGHT - 6.0, Colors["#101827"]) {
                    x = 3.0
                    y = 3.0
                }
                val bitmap = previews.getValue(map)
                val previewScale = min(176.0 / bitmap.width, 118.0 / bitmap.height)
                card.image(bitmap) {
                    x = 14.0 + (176.0 - bitmap.width * previewScale) / 2.0
                    y = (CARD_HEIGHT - bitmap.height * previewScale) / 2.0
                    scaleX = previewScale
                    scaleY = previewScale
                    smoothing = false
                }
                card.text(map.name, textSize = 22.0, color = Colors.WHITE) {
                    x = 206.0
                    y = 35.0
                }
                val selectedLabel =
                    card.text("Selected", textSize = 20.0, color = Colors["#F8D66D"]) {
                        x = 206.0
                        y = 86.0
                    }
                card.onClick {
                    audio.play(GameSound.MAP_SELECT)
                    controller.selectEndlessZone(map.zone)
                    refreshSelection()
                }
                val refresh: () -> Unit = {
                    val selected = controller.selectedEndlessZone == map.zone
                    border.color = if (selected) Colors["#F8D66D"] else Colors["#667085"]
                    selectedLabel.visible = selected
                }
                refresh
            }

        text("Survive as long as you can. No boss encounter.", textSize = 18.0, color = Colors.WHITE) {
            x = (screenWidth - width) / 2.0
            y = innerY + 432.0
        }
        createMedievalImageButton(
            x = innerX + PADDING,
            y = innerY + 464.0,
            width = 250.0,
            height = 60.0,
            texture = buttonBitmap,
            label = "Back",
            onClick = onBack,
        )
        createMedievalImageButton(
            x = innerX + innerWidth - PADDING - 300.0,
            y = innerY + 464.0,
            width = 300.0,
            height = 60.0,
            texture = buttonBitmap,
            label = "Start Endless",
            onClick = onStart,
        )

        refreshSelection = {
            difficultyLabel.text = "Choose your map  |  Difficulty: ${controller.activeDifficulty.displayName}"
            difficultyLabel.x = (screenWidth - difficultyLabel.width) / 2.0
            refreshCards.forEach { it() }
        }
        refreshSelection()
        var displayedZone = controller.selectedEndlessZone
        var displayedDifficulty = controller.activeDifficulty
        addUpdater {
            if (visible &&
                (displayedZone != controller.selectedEndlessZone || displayedDifficulty != controller.activeDifficulty)
            ) {
                displayedZone = controller.selectedEndlessZone
                displayedDifficulty = controller.activeDifficulty
                refreshSelection()
            }
        }
    }
}
