package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.state.GameScreenState
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

object DragonGameConfig {
    const val GAME_TITLE: String = "Dragon's First Flight"

    val VIRTUAL_SIZE: Size = Size(
        width = 1280.0,
        height = 720.0
    )

    const val GAME_AREA_X: Double = 50.0
    const val GAME_AREA_Y: Double = 230.0
    const val GAME_AREA_WIDTH: Double = 1180.0

    const val BUTTON_WIDTH: Double = 280.0
    const val BUTTON_HEIGHT: Double = 70.0
}

suspend fun Container.createDragonGame() {
    var screenState = GameScreenState.MENU

    val engine = GameEngine(
        gameAreaWidth = DragonGameConfig.GAME_AREA_WIDTH
    )

    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"]
    )

    val menuContainer = container()
    val gameContainer = container()

    createMainMenu(
        parent = menuContainer,
        onPlay = {
            engine.restart()
            screenState = GameScreenState.PLAYING
            menuContainer.visible = false
            gameContainer.visible = true
        }
    )

    createGameplayScreen(
        parent = gameContainer,
        engine = engine,
        isPlaying = {
            screenState == GameScreenState.PLAYING
        },
        onBackToMenu = {
            screenState = GameScreenState.MENU
            gameContainer.visible = false
            menuContainer.visible = true
        }
    )

    menuContainer.visible = true
    gameContainer.visible = false
}

private fun createMainMenu(
    parent: Container,
    onPlay: () -> Unit
) {
    with(parent) {
        text(
            text = DragonGameConfig.GAME_TITLE,
            textSize = 58.0,
            color = Colors.WHITE
        ) {
            x = 350.0
            y = 180.0
        }

        text(
            text = "A shared Kotlin Multiplatform game powered by KorGE",
            textSize = 24.0,
            color = Colors["#94A3B8"]
        ) {
            x = 315.0
            y = 270.0
        }

        createButton(
            x = (
                    DragonGameConfig.VIRTUAL_SIZE.width -
                            DragonGameConfig.BUTTON_WIDTH
                    ) / 2.0,
            y = 380.0,
            label = "Play",
            onClick = onPlay
        )

        text(
            text = "Click or tap to control the dragon",
            textSize = 21.0,
            color = Colors["#64748B"]
        ) {
            x = 460.0
            y = 500.0
        }
    }
}

private fun createGameplayScreen(
    parent: Container,
    engine: GameEngine,
    isPlaying: () -> Boolean,
    onBackToMenu: () -> Unit
) {
    with(parent) {
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

        createButton(
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
                if (!isPlaying()) {
                    return@onClick
                }

                if (engine.isRunning) {
                    engine.flap()
                } else {
                    engine.restart()
                }
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
            if (!isPlaying()) {
                return@addUpdater
            }

            val elapsedSeconds =
                delta.inWholeMilliseconds / 1000.0

            engine.update(elapsedSeconds)
            updateView(engine.snapshot())
        }

        updateView(engine.snapshot())
    }
}

private fun Container.createButton(
    x: Double,
    y: Double,
    width: Double = DragonGameConfig.BUTTON_WIDTH,
    height: Double = DragonGameConfig.BUTTON_HEIGHT,
    label: String,
    onClick: () -> Unit
) {
    val button = solidRect(
        width = width,
        height = height,
        color = Colors["#2563EB"]
    ) {
        this.x = x
        this.y = y
    }

    val buttonText = text(
        text = label,
        textSize = 26.0,
        color = Colors.WHITE
    )

    buttonText.x =
        x + (width - buttonText.width) / 2.0

    buttonText.y =
        y + (height - buttonText.height) / 2.0

    button.onClick {
        onClick()
    }

    buttonText.onClick {
        onClick()
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