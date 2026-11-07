package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.state.GameScreenState
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.random.Random

class GameController(
    gameAreaWidth: Double,
    private val coinWallet: CoinWallet = CoinWallet(InmemoryStorage()),
    random: Random = Random.Default,
) {
    private val engine =
        GameEngine(
            gameAreaWidth = gameAreaWidth,
            random = random,
        )

    private var bankedRunCoins = 0

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

    val coinBalance: Int
        get() = coinWallet.balance + (engine.runCoins - bankedRunCoins)

    fun startGame() {
        restartRun()
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    fun returnToMenu() {
        bankRunCoins()
        isPaused = false
        screenState = GameScreenState.MENU
    }

    fun pauseGame() {
        if (isGameVisible && engine.isRunning) {
            isPaused = true
            bankRunCoins()
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
            restartRun()
        }
    }

    fun update(elapsedSeconds: Double) {
        if (!isGameVisible || isPaused) {
            return
        }

        engine.update(elapsedSeconds)

        if (!engine.isRunning) {
            bankRunCoins()
        }
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

    private fun bankRunCoins() {
        val unbanked = engine.runCoins - bankedRunCoins
        if (unbanked > 0) {
            coinWallet.deposit(unbanked)
            bankedRunCoins = engine.runCoins
        }
    }

    private fun restartRun() {
        bankRunCoins()
        engine.restart()
        bankedRunCoins = 0
    }
}
