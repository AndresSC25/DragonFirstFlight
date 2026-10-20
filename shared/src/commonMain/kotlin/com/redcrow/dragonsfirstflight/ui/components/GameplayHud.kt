package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val MAX_LIVES: Int = 3
private const val LIFE_ICON_SIZE: Double = 42.0
private const val LIFE_ICON_GAP: Double = 8.0
private const val HUD_CONTENT_Y: Double = 62.0

internal class GameplayHud private constructor(
    private val fullLifeIcons: List<Image>,
    private val pointsText: Text,
    private val pauseButtonContainer: Container
) {
    private var displayedPoints: Int? = null

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean
    ) {
        fullLifeIcons.forEachIndexed { index, icon ->
            icon.visible = index < snapshot.lives
        }

        if (displayedPoints != snapshot.points) {
            pointsText.text = "Points: ${snapshot.points}"
            displayedPoints = snapshot.points
        }

        pauseButtonContainer.visible =
            snapshot.status == GameStatus.RUNNING &&
                !isPaused
    }

    companion object {
        fun create(
            container: Container,
            lifeFullBitmap: Bitmap,
            lifeEmptyBitmap: Bitmap,
            gameButtonBitmap: Bitmap,
            onPause: () -> Unit,
            onBackToMenu: () -> Unit
        ): GameplayHud {
            val fullLifeIcons = mutableListOf<Image>()

            container.solidRect(
                width = DragonGameConfig.HUD_WIDTH,
                height = DragonGameConfig.HUD_HEIGHT,
                color = Colors["#101827"]
            ) {
                x = DragonGameConfig.HUD_X
                y = DragonGameConfig.HUD_Y
                alpha = 0.88
            }

            container.solidRect(
                width = DragonGameConfig.HUD_WIDTH,
                height = 3.0,
                color = Colors["#C9962F"]
            ) {
                x = DragonGameConfig.HUD_X
                y =
                    DragonGameConfig.HUD_Y +
                        DragonGameConfig.HUD_HEIGHT -
                        3.0

                alpha = 0.95
            }

            container.text(
                text = "Lives",
                textSize = 17.0,
                color = Colors["#F8D66D"]
            ) {
                x = 45.0
                y = 31.0
            }

            repeat(MAX_LIVES) { index ->
                val iconX =
                    45.0 +
                        index *
                        (LIFE_ICON_SIZE + LIFE_ICON_GAP)

                container.image(
                    texture = lifeEmptyBitmap
                ) {
                    x = iconX
                    y = HUD_CONTENT_Y

                    scaleX =
                        LIFE_ICON_SIZE /
                            lifeEmptyBitmap.width.toDouble()

                    scaleY =
                        LIFE_ICON_SIZE /
                            lifeEmptyBitmap.height.toDouble()

                    smoothing = false
                }

                val fullIcon =
                    container.image(
                        texture = lifeFullBitmap
                    ) {
                        x = iconX
                        y = HUD_CONTENT_Y

                        scaleX =
                            LIFE_ICON_SIZE /
                                lifeFullBitmap.width.toDouble()

                        scaleY =
                            LIFE_ICON_SIZE /
                                lifeFullBitmap.height.toDouble()

                        smoothing = false
                    }

                fullLifeIcons += fullIcon
            }

            val pointsText =
                container.text(
                    text = "",
                    textSize = 25.0,
                    color = Colors.WHITE
                ) {
                    x = 235.0
                    y = 69.0
                }

            val pauseButtonContainer =
                container.container {
                    createMedievalImageButton(
                        x = 850.0,
                        y = 45.0,
                        width = 140.0,
                        height = 52.0,
                        texture = gameButtonBitmap,
                        label = "Pause",
                        onClick = onPause
                    )
                }

            container.createMedievalImageButton(
                x = 1010.0,
                y = 45.0,
                width = 220.0,
                height = 52.0,
                texture = gameButtonBitmap,
                label = "Back to menu",
                onClick = onBackToMenu
            )

            return GameplayHud(
                fullLifeIcons = fullLifeIcons,
                pointsText = pointsText,
                pauseButtonContainer = pauseButtonContainer
            )
        }
    }
}
