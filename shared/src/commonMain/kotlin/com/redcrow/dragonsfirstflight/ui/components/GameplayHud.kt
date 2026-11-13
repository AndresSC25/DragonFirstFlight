package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.text

private const val MAX_LIVES: Int = 3
private const val HUD_MARGIN: Double = 20.0
private const val HUD_ITEM_HEIGHT: Double = 48.0
private const val LIFE_ICON_GAP: Double = 6.0
private const val SCORE_PLATE_WIDTH: Double = 192.0
private const val SCORE_PLATE_GAP: Double = 16.0
private const val SCORE_TEXT_SIZE: Double = 24.0
private const val SCORE_TEXT_OFFSET_Y: Double = 3.0
private const val PAUSE_BUTTON_WIDTH: Double = 96.0
private const val COIN_PLATE_WIDTH: Double = 150.0
private const val COIN_ICON_SIZE: Double = 28.0
private const val COIN_ICON_PADDING: Double = 14.0
private const val COIN_TEXT_GAP: Double = 8.0

private const val SCORE_PLATE_X: Double =
    MAX_LIVES * HUD_ITEM_HEIGHT +
        (MAX_LIVES - 1) * LIFE_ICON_GAP +
        SCORE_PLATE_GAP

private const val COIN_PLATE_X: Double =
    SCORE_PLATE_X + SCORE_PLATE_WIDTH + SCORE_PLATE_GAP

internal class GameplayHud private constructor(
    private val views: Views,
    private val statusGroup: Container,
    private val fullLifeIcons: List<Image>,
    private val pointsText: Text,
    private val coinsText: Text,
    private val pauseButton: Image,
) {
    private var displayedPoints: Int? = null
    private var displayedCoins: Int? = null

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
    ) {
        anchorToViewportCorners()

        fullLifeIcons.forEachIndexed { index, icon ->
            icon.visible = index < snapshot.lives
        }

        if (displayedPoints != snapshot.points) {
            pointsText.text = "Points: ${snapshot.points}"
            pointsText.x = SCORE_PLATE_X + (SCORE_PLATE_WIDTH - pointsText.width) / 2.0
            displayedPoints = snapshot.points
        }

        if (displayedCoins != snapshot.runCoins) {
            coinsText.text = snapshot.runCoins.toString()
            displayedCoins = snapshot.runCoins
        }

        pauseButton.visible =
            snapshot.status == GameStatus.RUNNING &&
            !isPaused
    }

    private fun anchorToViewportCorners() {
        val top =
            views.actualVirtualTop.toDouble() + HUD_MARGIN

        val left =
            views.actualVirtualLeft.toDouble()

        statusGroup.x = left + HUD_MARGIN
        statusGroup.y = top

        pauseButton.x =
            left +
            views.actualVirtualWidth.toDouble() -
            HUD_MARGIN -
            PAUSE_BUTTON_WIDTH

        pauseButton.y = top
    }

    companion object {
        fun create(
            container: Container,
            views: Views,
            lifeFullBitmap: Bitmap,
            lifeEmptyBitmap: Bitmap,
            scorePlateBitmap: Bitmap,
            coinIcon: Bitmap,
            pauseButtonBitmap: Bitmap,
            onPause: () -> Unit,
        ): GameplayHud {
            val fullLifeIcons = mutableListOf<Image>()
            lateinit var pointsText: Text
            lateinit var coinsText: Text

            val statusGroup =
                container.container {
                    repeat(MAX_LIVES) { index ->
                        val iconX =
                            index * (HUD_ITEM_HEIGHT + LIFE_ICON_GAP)

                        hudImage(
                            bitmap = lifeEmptyBitmap,
                            x = iconX,
                            width = HUD_ITEM_HEIGHT,
                        )

                        fullLifeIcons +=
                            hudImage(
                                bitmap = lifeFullBitmap,
                                x = iconX,
                                width = HUD_ITEM_HEIGHT,
                            )
                    }

                    hudImage(
                        bitmap = scorePlateBitmap,
                        x = SCORE_PLATE_X,
                        width = SCORE_PLATE_WIDTH,
                    )

                    pointsText =
                        text(
                            text = "Points: 0",
                            textSize = SCORE_TEXT_SIZE,
                            color = Colors.WHITE,
                        )

                    pointsText.y =
                        (HUD_ITEM_HEIGHT - pointsText.height) / 2.0 + SCORE_TEXT_OFFSET_Y

                    hudImage(
                        bitmap = scorePlateBitmap,
                        x = COIN_PLATE_X,
                        width = COIN_PLATE_WIDTH,
                    )

                    image(texture = coinIcon) {
                        x = COIN_PLATE_X + COIN_ICON_PADDING
                        y = (HUD_ITEM_HEIGHT - COIN_ICON_SIZE) / 2.0
                        scaleX = COIN_ICON_SIZE / coinIcon.width.toDouble()
                        scaleY = COIN_ICON_SIZE / coinIcon.height.toDouble()
                        smoothing = true
                    }

                    coinsText =
                        text(
                            text = "0",
                            textSize = SCORE_TEXT_SIZE,
                            color = Colors.WHITE,
                        )

                    coinsText.x =
                        COIN_PLATE_X + COIN_ICON_PADDING + COIN_ICON_SIZE + COIN_TEXT_GAP

                    coinsText.y =
                        (HUD_ITEM_HEIGHT - coinsText.height) / 2.0 + SCORE_TEXT_OFFSET_Y
                }

            val pauseButton =
                container.hudImage(
                    bitmap = pauseButtonBitmap,
                    x = 0.0,
                    width = PAUSE_BUTTON_WIDTH,
                )

            pauseButton.onClick {
                onPause()
            }

            return GameplayHud(
                views = views,
                statusGroup = statusGroup,
                fullLifeIcons = fullLifeIcons,
                pointsText = pointsText,
                coinsText = coinsText,
                pauseButton = pauseButton,
            )
        }

        private fun Container.hudImage(
            bitmap: Bitmap,
            x: Double,
            width: Double,
        ): Image =
            image(texture = bitmap) {
                this.x = x
                scaleX = width / bitmap.width.toDouble()
                scaleY = HUD_ITEM_HEIGHT / bitmap.height.toDouble()
                smoothing = false
            }
    }
}
