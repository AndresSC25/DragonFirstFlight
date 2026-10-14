package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveBackdrop
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.sprite
import korlibs.korge.view.text
import korlibs.math.geom.Size
import korlibs.time.milliseconds

private const val DRAGON_SPRITESHEET_PATH: String =
    "sprites/dragon/YoungBrassDragon.png"

private const val ZONE_ONE_BACKGROUND_PATH: String =
    "backgrounds/zone1/fortress_courtyard.png"

private const val PAUSE_PANEL_PATH: String =
    "ui/panels/panel_medieval.png"

private const val GAME_BUTTON_PATH: String =
    "ui/buttons/button_normal.png"

private const val LIFE_FULL_PATH: String =
    "sprites/dragon/lives/dragon_life_full.png"

private const val LIFE_EMPTY_PATH: String =
    "sprites/dragon/lives/dragon_life_empty.png"

private const val DRAGON_FRAME_WIDTH: Int = 32
private const val DRAGON_FRAME_HEIGHT: Int = 32
private const val DRAGON_FRAME_COLUMNS: Int = 6
private const val DRAGON_FRAME_ROWS: Int = 1
private const val DRAGON_FRAME_DURATION_MS: Int = 120

private const val BACKGROUND_SCROLL_SPEED: Double = 70.0

private const val MAX_LIVES: Int = 3
private const val LIFE_ICON_SIZE: Double = 42.0
private const val LIFE_ICON_GAP: Double = 8.0
private const val HUD_CONTENT_Y: Double = 62.0

private const val PAUSE_PANEL_WIDTH: Double = 560.0
private const val PAUSE_PANEL_HEIGHT: Double = 430.0
private const val PAUSE_CONTENT_WIDTH: Double = 430.0
private const val PAUSE_CONTENT_HEIGHT: Double = 345.0

internal suspend fun Container.createGameplayScreen(
    views: Views,
    controller: GameController,
    onBackToMenu: () -> Unit
): Container {
    val zoneOneBackground =
        resourcesVfs[ZONE_ONE_BACKGROUND_PATH].readBitmap()

    val dragonSpriteMap =
        resourcesVfs[DRAGON_SPRITESHEET_PATH].readBitmap()

    val pausePanelBitmap =
        resourcesVfs[PAUSE_PANEL_PATH].readBitmap()

    val gameButtonBitmap =
        resourcesVfs[GAME_BUTTON_PATH].readBitmap()

    val lifeFullBitmap =
        resourcesVfs[LIFE_FULL_PATH].readBitmap()

    val lifeEmptyBitmap =
        resourcesVfs[LIFE_EMPTY_PATH].readBitmap()

    require(
        dragonSpriteMap.width ==
                DRAGON_FRAME_WIDTH * DRAGON_FRAME_COLUMNS
    ) {
        "Invalid dragon spritesheet width."
    }

    require(
        dragonSpriteMap.height == DRAGON_FRAME_HEIGHT
    ) {
        "Invalid dragon spritesheet height."
    }

    val dragonFlightAnimation = SpriteAnimation(
        spriteMap = dragonSpriteMap,
        spriteWidth = DRAGON_FRAME_WIDTH,
        spriteHeight = DRAGON_FRAME_HEIGHT,
        marginTop = 0,
        marginLeft = 0,
        columns = DRAGON_FRAME_COLUMNS,
        rows = DRAGON_FRAME_ROWS,
        offsetBetweenColumns = 0,
        offsetBetweenRows = 0
    )

    return container {
        lateinit var firstBackground: Image
        lateinit var secondBackground: Image
        lateinit var dragon: Sprite
        lateinit var obstacle: SolidRect
        lateinit var obstacleLabel: Text

        val fullLifeIcons = mutableListOf<Image>()
        val emptyLifeIcons = mutableListOf<Image>()
        var isDragonAnimationStopped = false

        val backgroundWidth =
            DragonGameConfig.GAME_AREA_WIDTH

        createResponsiveBackdrop(
            bitmap = zoneOneBackground,
            views = views
        )

        val gameArea = clipContainer(
            size = Size(
                width = DragonGameConfig.GAME_AREA_WIDTH,
                height = GameEngineConfig.GAME_AREA_HEIGHT
            )
        ) {
            x = DragonGameConfig.GAME_AREA_X
            y = DragonGameConfig.GAME_AREA_Y

            val backgroundScaleX =
                backgroundWidth /
                        zoneOneBackground.width.toDouble()

            val backgroundScaleY =
                GameEngineConfig.GAME_AREA_HEIGHT /
                        zoneOneBackground.height.toDouble()

            firstBackground = image(
                texture = zoneOneBackground
            ) {
                x = 0.0
                y = 0.0
                scaleX = backgroundScaleX
                scaleY = backgroundScaleY
                smoothing = false
            }

            secondBackground = image(
                texture = zoneOneBackground
            ) {
                x = backgroundWidth
                y = 0.0
                scaleX = backgroundScaleX
                scaleY = backgroundScaleY
                smoothing = false
            }

            dragon = sprite(
                initialAnimation = dragonFlightAnimation
            )

            val dragonScale =
                GameEngineConfig.DRAGON_SIZE /
                        DRAGON_FRAME_WIDTH.toDouble()

            dragon.scaleX = dragonScale
            dragon.scaleY = dragonScale
            dragon.smoothing = false

            dragon.playAnimationLooped(
                spriteAnimation = dragonFlightAnimation,
                spriteDisplayTime =
                    DRAGON_FRAME_DURATION_MS.milliseconds
            )

            obstacle = solidRect(
                width = GameEngineConfig.OBSTACLE_SIZE,
                height = GameEngineConfig.OBSTACLE_SIZE,
                color = Colors["#E53935"]
            )

            obstacleLabel = text(
                text = "X",
                textSize = 36.0,
                color = Colors.WHITE
            )
        }

        gameArea.onClick {
            controller.handleGameAreaInput()
        }

        solidRect(
            width = DragonGameConfig.HUD_WIDTH,
            height = DragonGameConfig.HUD_HEIGHT,
            color = Colors["#101827"]
        ) {
            x = DragonGameConfig.HUD_X
            y = DragonGameConfig.HUD_Y
            alpha = 0.88
        }

        solidRect(
            width = DragonGameConfig.HUD_WIDTH,
            height = 3.0,
            color = Colors["#C9962F"]
        ) {
            x = DragonGameConfig.HUD_X
            y = DragonGameConfig.HUD_Y + DragonGameConfig.HUD_HEIGHT - 3.0
            alpha = 0.95
        }

        text(
            text = "Lives",
            textSize = 17.0,
            color = Colors["#F8D66D"]
        ) {
            x = 45.0
            y = 31.0
        }

        repeat(MAX_LIVES) { index ->
            val iconX =
                45.0 + index * (LIFE_ICON_SIZE + LIFE_ICON_GAP)

            val emptyIcon = image(texture = lifeEmptyBitmap) {
                x = iconX
                y = HUD_CONTENT_Y
                scaleX = LIFE_ICON_SIZE / lifeEmptyBitmap.width.toDouble()
                scaleY = LIFE_ICON_SIZE / lifeEmptyBitmap.height.toDouble()
                smoothing = false
            }

            val fullIcon = image(texture = lifeFullBitmap) {
                x = iconX
                y = HUD_CONTENT_Y
                scaleX = LIFE_ICON_SIZE / lifeFullBitmap.width.toDouble()
                scaleY = LIFE_ICON_SIZE / lifeFullBitmap.height.toDouble()
                smoothing = false
            }

            emptyLifeIcons += emptyIcon
            fullLifeIcons += fullIcon
        }

        val pointsText = text(
            text = "",
            textSize = 25.0,
            color = Colors.WHITE
        ) {
            x = 235.0
            y = 69.0
        }

        val pauseButtonContainer = container {
            createGameplayImageButton(
                x = 850.0,
                y = 45.0,
                width = 140.0,
                height = 52.0,
                texture = gameButtonBitmap,
                label = "Pause",
                onClick = controller::pauseGame
            )
        }

        createGameplayImageButton(
            x = 1010.0,
            y = 45.0,
            width = 220.0,
            height = 52.0,
            texture = gameButtonBitmap,
            label = "Back to menu",
            onClick = onBackToMenu
        )

        val resultText = text(
            text = "",
            textSize = 38.0,
            color = Colors["#FBBF24"]
        ) {
            x = 40.0
            y = 610.0
        }

        val restartText = text(
            text = "",
            textSize = 24.0,
            color = Colors.WHITE
        ) {
            x = 40.0
            y = 660.0
        }

        val pauseContainer = container {
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
                    (DragonGameConfig.VIRTUAL_SIZE.width -
                            PAUSE_CONTENT_WIDTH) / 2.0
                y = 170.0
                alpha = 0.70
            }

            image(texture = pausePanelBitmap) {
                x =
                    (DragonGameConfig.VIRTUAL_SIZE.width -
                            PAUSE_PANEL_WIDTH) / 2.0
                y =
                    (DragonGameConfig.VIRTUAL_SIZE.height -
                            PAUSE_PANEL_HEIGHT) / 2.0
                scaleX =
                    PAUSE_PANEL_WIDTH /
                            pausePanelBitmap.width.toDouble()
                scaleY =
                    PAUSE_PANEL_HEIGHT /
                            pausePanelBitmap.height.toDouble()
                smoothing = false
            }

            val pauseTitle = text(
                text = "Paused",
                textSize = 48.0,
                color = Colors["#F8D66D"]
            )
            pauseTitle.x =
                (DragonGameConfig.VIRTUAL_SIZE.width -
                        pauseTitle.width) / 2.0
            pauseTitle.y = 230.0

            val pauseSubtitle = text(
                text = "Your flight is waiting",
                textSize = 19.0,
                color = Colors.WHITE
            )
            pauseSubtitle.x =
                (DragonGameConfig.VIRTUAL_SIZE.width -
                        pauseSubtitle.width) / 2.0
            pauseSubtitle.y = 295.0

            createGameplayImageButton(
                x = 500.0,
                y = 345.0,
                width = 280.0,
                height = 64.0,
                texture = gameButtonBitmap,
                label = "Resume",
                onClick = controller::resumeGame
            )

            createGameplayImageButton(
                x = 500.0,
                y = 435.0,
                width = 280.0,
                height = 64.0,
                texture = gameButtonBitmap,
                label = "Back to menu",
                onClick = onBackToMenu
            )
        }

        fun updateView(snapshot: GameSnapshot) {
            dragon.x = snapshot.dragonX
            dragon.y = snapshot.dragonY

            obstacle.x = snapshot.obstacleX
            obstacle.y = snapshot.obstacleY

            obstacleLabel.x =
                obstacle.x +
                        (snapshot.obstacleSize - obstacleLabel.width) / 2.0

            obstacleLabel.y =
                obstacle.y +
                        (snapshot.obstacleSize - obstacleLabel.height) / 2.0
            fullLifeIcons.forEachIndexed { index, icon ->
                icon.visible = index < snapshot.lives
            }

            emptyLifeIcons.forEach { icon ->
                icon.visible = true
            }

            pointsText.text =
                "Points: ${snapshot.points}/" +
                        GameEngineConfig.POINTS_TO_WIN

            pauseContainer.visible = controller.isPaused
            pauseButtonContainer.visible =
                snapshot.status == GameStatus.RUNNING &&
                        !controller.isPaused

            when (snapshot.status) {
                GameStatus.RUNNING -> {
                    resultText.text = ""
                    restartText.text = ""
                }

                GameStatus.WON -> {
                    resultText.text =
                        "Victory! You avoided " +
                                "${GameEngineConfig.POINTS_TO_WIN} obstacles."
                    restartText.text =
                        "Click or tap to play again"
                }

                GameStatus.LOST -> {
                    resultText.text = "Game over"
                    restartText.text =
                        "Click or tap to play again"
                }
            }
        }
        fun updateDragonAnimation(snapshot: GameSnapshot) {
            val shouldAnimate =
                snapshot.status == GameStatus.RUNNING &&
                        !controller.isPaused

            if (shouldAnimate && isDragonAnimationStopped) {
                dragon.playAnimationLooped(
                    spriteAnimation = dragonFlightAnimation,
                    spriteDisplayTime =
                        DRAGON_FRAME_DURATION_MS.milliseconds
                )
                isDragonAnimationStopped = false
            } else if (!shouldAnimate && !isDragonAnimationStopped) {
                dragon.stopAnimation()
                isDragonAnimationStopped = true
            }
        }


        fun updateScrollingBackground(elapsedSeconds: Double) {
            val movement =
                BACKGROUND_SCROLL_SPEED * elapsedSeconds

            firstBackground.x -= movement
            secondBackground.x -= movement

            if (firstBackground.x <= -backgroundWidth) {
                firstBackground.x =
                    secondBackground.x + backgroundWidth
            }

            if (secondBackground.x <= -backgroundWidth) {
                secondBackground.x =
                    firstBackground.x + backgroundWidth
            }
        }

        gameArea.addUpdater { delta ->
            val elapsedSeconds =
                delta.inWholeMilliseconds / 1000.0

            controller.update(elapsedSeconds)

            val snapshot = controller.snapshot()

            if (
                snapshot.status == GameStatus.RUNNING &&
                !controller.isPaused
            ) {
                updateScrollingBackground(elapsedSeconds)
            }
            updateDragonAnimation(snapshot)
            updateView(snapshot)
        }

        val initialSnapshot = controller.snapshot()
        updateDragonAnimation(initialSnapshot)
        updateView(initialSnapshot)
    }
}

private fun Container.createGameplayImageButton(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    texture: Bitmap,
    label: String,
    onClick: () -> Unit
): Image {
    require(width > 0.0) {
        "Button width must be greater than zero."
    }
    require(height > 0.0) {
        "Button height must be greater than zero."
    }

    val button = image(texture = texture) {
        this.x = x
        this.y = y
        scaleX = width / texture.width.toDouble()
        scaleY = height / texture.height.toDouble()
        smoothing = false
    }

    val buttonText = text(
        text = label,
        textSize = 24.0,
        color = Colors.WHITE
    )
    buttonText.x = x + (width - buttonText.width) / 2.0
    buttonText.y = y + (height - buttonText.height) / 2.0 + 3.0

    button.onClick { onClick() }
    buttonText.onClick { onClick() }

    return button
}


