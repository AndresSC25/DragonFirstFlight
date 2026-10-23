package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val PANEL_WIDTH: Double = 560.0
private const val PANEL_HEIGHT: Double = 430.0
private const val CONTENT_WIDTH: Double = 430.0
private const val CONTENT_HEIGHT: Double = 345.0

internal class SessionOverlay private constructor(
    private val root: Container,
    private val title: Text,
    private val subtitle: Text,
    private val resumeButton: Container,
    private val restartButton: Container,
) {
    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
    ) {
        val hasFinished =
            snapshot.status != GameStatus.RUNNING

        root.visible =
            isPaused || hasFinished

        if (!root.visible) {
            return
        }

        val titleText =
            when (snapshot.status) {
                GameStatus.RUNNING -> {
                    "Paused"
                }

                GameStatus.LOST -> {
                    "Game Over"
                }

                GameStatus.WON -> {
                    "Victory!"
                }
            }

        val subtitleText =
            if (hasFinished) {
                "Final score: ${snapshot.points}"
            } else {
                "Your flight is waiting"
            }

        if (title.text != titleText) {
            title.text = titleText

            title.x =
                (
                    DragonGameConfig.VIRTUAL_SIZE.width -
                        title.width
                ) / 2.0
        }

        if (subtitle.text != subtitleText) {
            subtitle.text = subtitleText

            subtitle.x =
                (
                    DragonGameConfig.VIRTUAL_SIZE.width -
                        subtitle.width
                ) / 2.0
        }

        resumeButton.visible =
            !hasFinished

        restartButton.visible =
            hasFinished
    }

    companion object {
        fun create(
            container: Container,
            views: Views,
            panelBitmap: Bitmap,
            buttonBitmap: Bitmap,
            onResume: () -> Unit,
            onRestart: () -> Unit,
            onBackToMenu: () -> Unit,
        ): SessionOverlay {
            require(panelBitmap.width > 0) {
                "Session panel width must be greater than zero."
            }

            require(panelBitmap.height > 0) {
                "Session panel height must be greater than zero."
            }

            require(buttonBitmap.width > 0) {
                "Session button width must be greater than zero."
            }

            require(buttonBitmap.height > 0) {
                "Session button height must be greater than zero."
            }

            lateinit var title: Text
            lateinit var subtitle: Text
            lateinit var resumeButton: Container
            lateinit var restartButton: Container

            val root =
                container.container {
                    visible = false

                    /*
                     * Esta capa cubre dinámicamente todo el viewport,
                     * incluidos los laterales visibles fuera de 1280 x 720.
                     */
                    createResponsiveShade(
                        views = views,
                        color = Colors.BLACK,
                        shadeAlpha = 0.62,
                    )

                    /*
                     * El contenido central permanece dentro del área
                     * virtual original.
                     */
                    solidRect(
                        width = CONTENT_WIDTH,
                        height = CONTENT_HEIGHT,
                        color = Colors["#101827"],
                    ) {
                        x =
                            (
                                DragonGameConfig.VIRTUAL_SIZE.width -
                                    CONTENT_WIDTH
                            ) / 2.0

                        y = 170.0
                        alpha = 0.70
                    }

                    image(texture = panelBitmap) {
                        x =
                            (
                                DragonGameConfig.VIRTUAL_SIZE.width -
                                    PANEL_WIDTH
                            ) / 2.0

                        y =
                            (
                                DragonGameConfig.VIRTUAL_SIZE.height -
                                    PANEL_HEIGHT
                            ) / 2.0

                        scaleX =
                            PANEL_WIDTH /
                            panelBitmap.width.toDouble()

                        scaleY =
                            PANEL_HEIGHT /
                            panelBitmap.height.toDouble()

                        smoothing = false
                    }

                    title =
                        text(
                            text = "Paused",
                            textSize = 48.0,
                            color = Colors["#F8D66D"],
                        )

                    title.x =
                        (
                            DragonGameConfig.VIRTUAL_SIZE.width -
                                title.width
                        ) / 2.0

                    title.y = 230.0

                    subtitle =
                        text(
                            text = "Your flight is waiting",
                            textSize = 19.0,
                            color = Colors.WHITE,
                        )

                    subtitle.x =
                        (
                            DragonGameConfig.VIRTUAL_SIZE.width -
                                subtitle.width
                        ) / 2.0

                    subtitle.y = 295.0

                    resumeButton =
                        container {
                            createMedievalImageButton(
                                x = 500.0,
                                y = 345.0,
                                width = 280.0,
                                height = 64.0,
                                texture = buttonBitmap,
                                label = "Resume",
                                onClick = onResume,
                            )
                        }

                    restartButton =
                        container {
                            visible = false

                            createMedievalImageButton(
                                x = 500.0,
                                y = 345.0,
                                width = 280.0,
                                height = 64.0,
                                texture = buttonBitmap,
                                label = "Play again",
                                onClick = onRestart,
                            )
                        }

                    createMedievalImageButton(
                        x = 500.0,
                        y = 435.0,
                        width = 280.0,
                        height = 64.0,
                        texture = buttonBitmap,
                        label = "Back to menu",
                        onClick = onBackToMenu,
                    )
                }

            return SessionOverlay(
                root = root,
                title = title,
                subtitle = subtitle,
                resumeButton = resumeButton,
                restartButton = restartButton,
            )
        }
    }
}
