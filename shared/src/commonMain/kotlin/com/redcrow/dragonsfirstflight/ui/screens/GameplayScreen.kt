package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.addUpdater
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Size

internal fun Container.createGameplayScreen(
    controller: GameController,
    onBackToMenu: () -> Unit
): Container {
    return container {
        text(
            text = DragonGameConfig.GAME_TITLE,
            textSize = 46.0,
            color = Colors.WHITE
        ) {
            x = 50.0
            y = 35.0
        }

        val statusText = text(
            text = "",
            textSize = 26.0,
            color = Colors["#CBD5E1"]
        ) {
            x = 50.0
            y = 110.0
        }

        val instructionText = text(
            text = "Click or tap inside the area to flap",
            textSize = 22.0,
            color = Colors["#94A3B8"]
        ) {
            x = 50.0
            y = 160.0
        }

        createGameButton(
            x = 980.0,
            y = 90.0,
            width = 250.0,
            height = 58.0,
            label = "Back to menu",
            onClick = onBackToMenu
        )

        lateinit var dragon: SolidRect
        lateinit var dragonLabel: Text
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

            val gameAreaBackground = solidRect(
                width = DragonGameConfig.GAME_AREA_WIDTH,
                height = GameEngineConfig.GAME_AREA_HEIGHT,
                color = Colors["#D9ECFF"]
            )

            dragon = solidRect(
                width = GameEngineConfig.DRAGON_SIZE,
                height = GameEngineConfig.DRAGON_SIZE,
                color = Colors["#1976D2"]
            )

            dragonLabel = text(
                text = "D",
                textSize = 30.0,
                color = Colors.WHITE
            )

            obstacle = solidRect(
                width = GameEngineConfig.OBSTACLE_SIZE,
                height = GameEngineConfig.OBSTACLE_SIZE,
                color = Colors["#E53935"]
            )

            obstacleLabel = text(
                text = "X",
                textSize = 28.0,
                color = Colors.WHITE
            )

            gameAreaBackground.onClick {
                controller.handleGameAreaInput()
            }
        }

        val resultText = text(
            text = "",
            textSize = 38.0,
            color = Colors["#FBBF24"]
        ) {
            x = 50.0
            y = 525.0
        }

        val restartText = text(
            text = "",
            textSize = 24.0,
            color = Colors["#CBD5E1"]
        ) {
            x = 50.0
            y = 585.0
        }

        fun updateView(snapshot: GameSnapshot) {
            updateDragonView(
                dragon = dragon,
                label = dragonLabel,
                snapshot = snapshot
            )

            updateObstacleView(
                obstacle = obstacle,
                label = obstacleLabel,
                snapshot = snapshot
            )

            statusText.text =
                "Lives: ${snapshot.lives}    " +
                        "Points: ${snapshot.points}/" +
                        GameEngineConfig.POINTS_TO_WIN

            when (snapshot.status) {
                GameStatus.RUNNING -> {
                    resultText.text = ""
                    restartText.text = ""

                    instructionText.text =
                        "Click or tap inside the area to flap"
                }

                GameStatus.WON -> {
                    resultText.text =
                        "Victory! You avoided " +
                                "${GameEngineConfig.POINTS_TO_WIN} obstacles."

                    restartText.text =
                        "Click or tap inside the area to play again"

                    instructionText.text = ""
                }

                GameStatus.LOST -> {
                    resultText.text = "Game over"

                    restartText.text =
                        "Click or tap inside the area to play again"

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

private fun updateDragonView(
    dragon: SolidRect,
    label: Text,
    snapshot: GameSnapshot
) {
    dragon.x = snapshot.dragonX
    dragon.y = snapshot.dragonY

    label.x =
        dragon.x +
                (snapshot.dragonSize - label.width) / 2.0

    label.y =
        dragon.y +
                (snapshot.dragonSize - label.height) / 2.0
}

private fun updateObstacleView(
    obstacle: SolidRect,
    label: Text,
    snapshot: GameSnapshot
) {
    obstacle.x = snapshot.obstacleX
    obstacle.y = snapshot.obstacleY

    label.x =
        obstacle.x +
                (snapshot.obstacleSize - label.width) / 2.0

    label.y =
        obstacle.y +
                (snapshot.obstacleSize - label.height) / 2.0
}
