package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.state.GameScreenState

class GameController(
    gameAreaWidth: Double
) {
    private val engine = GameEngine(
        gameAreaWidth = gameAreaWidth
    )

    var screenState: GameScreenState =
        GameScreenState.MENU
        private set

    val isMenuVisible: Boolean
        get() = screenState == GameScreenState.MENU

    val isGameVisible: Boolean
        get() = screenState == GameScreenState.PLAYING

    val isGameRunning: Boolean
        get() = isGameVisible && engine.isRunning

    fun startGame() {
        engine.restart()
        screenState = GameScreenState.PLAYING
    }

    fun returnToMenu() {
        screenState = GameScreenState.MENU
    }

    fun handleGameAreaInput() {
        if (!isGameVisible) {
            return
        }

        if (engine.isRunning) {
            engine.flap()
        } else {
            engine.restart()
        }
    }

    fun update(elapsedSeconds: Double) {
        if (!isGameVisible) {
            return
        }

        engine.update(elapsedSeconds)
    }

    fun resizeGameArea(width: Double) {
        engine.resizeGameArea(width)
    }

    fun snapshot(): GameSnapshot {
        return engine.snapshot()
    }
}