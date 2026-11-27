package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameMode
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
private const val PANEL_HEIGHT: Double = 520.0
private const val CONTENT_WIDTH: Double = 430.0
private const val CONTENT_HEIGHT: Double = 435.0

/** Pause and end-of-run overlay with resume, restart, continue to endless and back to menu buttons. */
internal class SessionOverlay private constructor(
    private val root: Container,
    private val title: Text,
    private val subtitle: Text,
    private val coinsLine: Text,
    private val resumeButton: Container,
    private val restartButton: Container,
    private val continueButton: Container,
    private val backToMenuButton: Container,
) {
    private var displayedRunCoins: Int? = null
    private var displayedCoinBalance: Int? = null

    /** Shows or hides the overlay and sets its title, score text and buttons from [snapshot]; call once per frame. */
    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
        coinBalance: Int,
    ) {
        val hasFinished =
            snapshot.status != GameStatus.RUNNING

        root.visible =
            isPaused ||
            hasFinished

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

        coinsLine.visible = hasFinished && !isPaused
        if (coinsLine.visible && (displayedRunCoins != snapshot.runCoins || displayedCoinBalance != coinBalance)) {
            displayedRunCoins = snapshot.runCoins
            displayedCoinBalance = coinBalance
            coinsLine.text = "Coins: +${snapshot.runCoins}   Total: $coinBalance"
            coinsLine.x = (DragonGameConfig.VIRTUAL_SIZE.width - coinsLine.width) / 2.0
        }

        resumeButton.visible =
            !hasFinished

        restartButton.visible =
            hasFinished
        val canContinue = snapshot.status == GameStatus.WON && snapshot.mode == GameMode.NORMAL
        continueButton.visible = canContinue
        restartButton.y = if (canContinue) 85.0 else 0.0
        backToMenuButton.y = if (canContinue) 85.0 else 0.0
    }

    companion object {
        fun create(
            container: Container,
            views: Views,
            panelBitmap: Bitmap,
            buttonBitmap: Bitmap,
            onResume: () -> Unit,
            onRestart: () -> Unit,
            onContinueEndless: () -> Unit,
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
            lateinit var coinsLine: Text
            lateinit var resumeButton: Container
            lateinit var restartButton: Container
            lateinit var continueButton: Container
            lateinit var backToMenuButton: Container

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

                        y = 145.0
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

                    title.y = 185.0

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

                    subtitle.y = 247.0

                    coinsLine =
                        text(
                            text = "",
                            textSize = 17.0,
                            color = Colors["#F8D66D"],
                        )

                    coinsLine.y = 271.0
                    coinsLine.visible = false

                    resumeButton =
                        container {
                            createMedievalImageButton(
                                x = 500.0,
                                y = 315.0,
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
                                y = 315.0,
                                width = 280.0,
                                height = 64.0,
                                texture = buttonBitmap,
                                label = "Play again",
                                onClick = onRestart,
                            )
                        }

                    continueButton =
                        container {
                            visible = false
                            createMedievalImageButton(
                                x = 500.0,
                                y = 315.0,
                                width = 280.0,
                                height = 64.0,
                                texture = buttonBitmap,
                                label = "Continue Endless",
                                onClick = onContinueEndless,
                            )
                        }
                    backToMenuButton =
                        container {
                            createMedievalImageButton(
                                x = 500.0,
                                y = 400.0,
                                width = 280.0,
                                height = 64.0,
                                texture = buttonBitmap,
                                label = "Back to menu",
                                onClick = onBackToMenu,
                            )
                        }
                }

            return SessionOverlay(
                root = root,
                title = title,
                subtitle = subtitle,
                coinsLine = coinsLine,
                resumeButton = resumeButton,
                restartButton = restartButton,
                continueButton = continueButton,
                backToMenuButton = backToMenuButton,
            )
        }
    }
}
