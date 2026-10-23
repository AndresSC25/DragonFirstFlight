package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.state.GameScreenState

class GameController(
    gameAreaWidth: Double,
) {
    private val engine =
        GameEngine(
            gameAreaWidth = gameAreaWidth,
        )

    var screenState: GameScreenState =
        GameScreenState.MENU
        private set

    var isPaused: Boolean = false
        private set

    val isMenuVisible: Boolean
        get() = screenState == GameScreenState.MENU

    val isGameVisible: Boolean
        get() = screenState == GameScreenState.PLAYING

    val isGameRunning: Boolean
        get() = isGameVisible && engine.isRunning && !isPaused

    fun startGame() {
        engine.restart()
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    fun returnToMenu() {
        isPaused = false
        screenState = GameScreenState.MENU
    }

    fun pauseGame() {
        if (isGameVisible && engine.isRunning) {
            isPaused = true
        }
    }

    fun resumeGame() {
        if (isGameVisible && engine.isRunning) {
            isPaused = false
        }
    }

    fun togglePause() {
        if (isPaused) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    fun handleGameAreaInput() {
        if (!isGameVisible || isPaused) {
            return
        }

        if (engine.isRunning) {
            engine.flap()
        } else {
            engine.restart()
        }
    }

    fun update(elapsedSeconds: Double) {
        if (!isGameVisible || isPaused) {
            return
        }

        engine.update(elapsedSeconds)
    }

    fun resizeGameArea(
        leftBoundary: Double,
        rightBoundary: Double,
    ) {
        engine.resizeGameArea(
            leftBoundary = leftBoundary,
            rightBoundary = rightBoundary,
        )
    }

    fun snapshot(): GameSnapshot = engine.snapshot()
}
