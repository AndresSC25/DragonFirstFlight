package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val PAUSE_PANEL_WIDTH: Double = 560.0
private const val PAUSE_PANEL_HEIGHT: Double = 430.0
private const val PAUSE_CONTENT_WIDTH: Double = 430.0
private const val PAUSE_CONTENT_HEIGHT: Double = 345.0

internal class PauseOverlay private constructor(
    private val root: Container
) {
    var isVisible: Boolean
        get() = root.visible
        set(value) {
            root.visible = value
        }

    companion object {
        fun create(
            container: Container,
            panelBitmap: Bitmap,
            buttonBitmap: Bitmap,
            onResume: () -> Unit,
            onBackToMenu: () -> Unit
        ): PauseOverlay {
            require(panelBitmap.width > 0) {
                "Pause panel width must be greater than zero."
            }

            require(panelBitmap.height > 0) {
                "Pause panel height must be greater than zero."
            }

            require(buttonBitmap.width > 0) {
                "Pause button width must be greater than zero."
            }

            require(buttonBitmap.height > 0) {
                "Pause button height must be greater than zero."
            }

            val root = container.container {
                visible = false

                solidRect(
                    width = DragonGameConfig.VIRTUAL_SIZE.width,
                    height = DragonGameConfig.VIRTUAL_SIZE.height,
                    color = Colors.BLACK
                ) {
                    alpha = 0.62
                }

                solidRect(
                    width = PAUSE_CONTENT_WIDTH,
                    height = PAUSE_CONTENT_HEIGHT,
                    color = Colors["#101827"]
                ) {
                    x =
                        (
                            DragonGameConfig.VIRTUAL_SIZE.width -
                                PAUSE_CONTENT_WIDTH
                            ) / 2.0

                    y = 170.0
                    alpha = 0.70
                }

                image(texture = panelBitmap) {
                    x =
                        (
                            DragonGameConfig.VIRTUAL_SIZE.width -
                                PAUSE_PANEL_WIDTH
                            ) / 2.0

                    y =
                        (
                            DragonGameConfig.VIRTUAL_SIZE.height -
                                PAUSE_PANEL_HEIGHT
                            ) / 2.0

                    scaleX =
                        PAUSE_PANEL_WIDTH /
                            panelBitmap.width.toDouble()

                    scaleY =
                        PAUSE_PANEL_HEIGHT /
                            panelBitmap.height.toDouble()

                    smoothing = false
                }

                val pauseTitle = text(
                    text = "Paused",
                    textSize = 48.0,
                    color = Colors["#F8D66D"]
                )

                pauseTitle.x =
                    (
                        DragonGameConfig.VIRTUAL_SIZE.width -
                            pauseTitle.width
                        ) / 2.0

                pauseTitle.y = 230.0

                val pauseSubtitle = text(
                    text = "Your flight is waiting",
                    textSize = 19.0,
                    color = Colors.WHITE
                )

                pauseSubtitle.x =
                    (
                        DragonGameConfig.VIRTUAL_SIZE.width -
                            pauseSubtitle.width
                        ) / 2.0

                pauseSubtitle.y = 295.0

                createMedievalImageButton(
                    x = 500.0,
                    y = 345.0,
                    width = 280.0,
                    height = 64.0,
                    texture = buttonBitmap,
                    label = "Resume",
                    onClick = onResume
                )

                createMedievalImageButton(
                    x = 500.0,
                    y = 435.0,
                    width = 280.0,
                    height = 64.0,
                    texture = buttonBitmap,
                    label = "Back to menu",
                    onClick = onBackToMenu
                )
            }

            return PauseOverlay(root = root)
        }
    }
}
