package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Text
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

private const val DRAGON_FRAME_WIDTH: Int = 32
private const val DRAGON_FRAME_HEIGHT: Int = 32
private const val DRAGON_FRAME_COLUMNS: Int = 6
private const val DRAGON_FRAME_ROWS: Int = 1
private const val DRAGON_FRAME_DURATION_MS: Int = 120

internal suspend fun Container.createGameplayScreen(
    controller: GameController,
    onBackToMenu: () -> Unit
): Container {
    val zoneOneBackground =
        resourcesVfs[ZONE_ONE_BACKGROUND_PATH].readBitmap()

    val dragonSpriteMap =
        resourcesVfs[DRAGON_SPRITESHEET_PATH].readBitmap()

    require(
        dragonSpriteMap.width ==
                DRAGON_FRAME_WIDTH * DRAGON_FRAME_COLUMNS
    ) {
        "Invalid dragon spritesheet width."
    }

    require(
        dragonSpriteMap.height ==
                DRAGON_FRAME_HEIGHT
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
        lateinit var dragon: Sprite
        lateinit var obstacle: SolidRect
        lateinit var obstacleLabel: Text

        val gameArea = clipContainer(
            size = Size(
                width = DragonGameConfig.GAME_AREA_WIDTH,
                height = GameEngineConfig.GAME_AREA_HEIGHT
            )
        ) {
            x = DragonGameConfig.GAME_AREA_X
            y = DragonGameConfig.GAME_AREA_Y

            val backgroundScaleX =
                DragonGameConfig.GAME_AREA_WIDTH /
                        zoneOneBackground.width.toDouble()

            val backgroundScaleY =
                GameEngineConfig.GAME_AREA_HEIGHT /
                        zoneOneBackground.height.toDouble()

            val gameAreaBackground = image(
                texture = zoneOneBackground
            ) {
                x = 0.0
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

            gameAreaBackground.onClick {
                controller.handleGameAreaInput()
            }
        }

        solidRect(
            width = DragonGameConfig.HUD_WIDTH,
            height = DragonGameConfig.HUD_HEIGHT,
            color = Colors["#101827"]
        ) {
            x = DragonGameConfig.HUD_X
            y = DragonGameConfig.HUD_Y
            alpha = 0.82
        }

        text(
            text = DragonGameConfig.GAME_TITLE,
            textSize = 34.0,
            color = Colors.WHITE
        ) {
            x = 45.0
            y = 28.0
        }

        val statusText = text(
            text = "",
            textSize = 22.0,
            color = Colors.WHITE
        ) {
            x = 45.0
            y = 78.0
        }

        val instructionText = text(
            text = "Click or tap to flap",
            textSize = 18.0,
            color = Colors["#CBD5E1"]
        ) {
            x = 45.0
            y = 112.0
        }

        createGameButton(
            x = 1010.0,
            y = 42.0,
            width = 220.0,
            height = 52.0,
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

        fun updateView(snapshot: GameSnapshot) {
            dragon.x = snapshot.dragonX
            dragon.y = snapshot.dragonY

            obstacle.x = snapshot.obstacleX
            obstacle.y = snapshot.obstacleY

            obstacleLabel.x =
                obstacle.x +
                        (
                                snapshot.obstacleSize -
                                        obstacleLabel.width
                                ) / 2.0

            obstacleLabel.y =
                obstacle.y +
                        (
                                snapshot.obstacleSize -
                                        obstacleLabel.height
                                ) / 2.0

            statusText.text =
                "Lives: ${snapshot.lives}    " +
                        "Points: ${snapshot.points}/" +
                        GameEngineConfig.POINTS_TO_WIN

            when (snapshot.status) {
                GameStatus.RUNNING -> {
                    resultText.text = ""
                    restartText.text = ""
                    instructionText.text =
                        "Click or tap to flap"
                }

                GameStatus.WON -> {
                    resultText.text =
                        "Victory! You avoided " +
                                "${GameEngineConfig.POINTS_TO_WIN} obstacles."

                    restartText.text =
                        "Click or tap to play again"

                    instructionText.text = ""
                }

                GameStatus.LOST -> {
                    resultText.text = "Game over"
                    restartText.text =
                        "Click or tap to play again"
                    instructionText.text = ""
                }
            }
        }

        gameArea.addUpdater { delta ->
            val elapsedSeconds =
                delta.inWholeMilliseconds / 1000.0

            controller.update(elapsedSeconds)
            updateView(controller.snapshot())
        }

        updateView(controller.snapshot())
    }
}