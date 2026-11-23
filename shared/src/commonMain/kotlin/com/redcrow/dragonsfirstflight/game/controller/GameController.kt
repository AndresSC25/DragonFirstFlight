package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.GameZone
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

    var activeDifficulty: GameDifficulty = GameDifficulty.EASY
        private set

    var selectedEndlessZone: GameZone = GameZone.ZONE_ONE
        private set

    var runNumber: Int = 0
        private set

    var screenState: GameScreenState =
        GameScreenState.MENU
        private set

    var isPaused: Boolean = false
        private set

    val isMenuVisible: Boolean
        get() = screenState == GameScreenState.MENU

    val isInventoryVisible: Boolean
        get() = screenState == GameScreenState.INVENTORY

    val isEndlessSetupVisible: Boolean
        get() = screenState == GameScreenState.ENDLESS_SETUP

    val isShopVisible: Boolean
        get() = screenState == GameScreenState.SHOP

    val isGameVisible: Boolean
        get() = screenState == GameScreenState.PLAYING

    val isGameRunning: Boolean
        get() = isGameVisible && engine.isRunning && !isPaused

    val canContinueEndless: Boolean
        get() = isGameVisible && engine.mode == GameMode.NORMAL && engine.status == GameStatus.WON

    val coinBalance: Int
        get() = coinWallet.balance + (engine.runCoins - bankedRunCoins)

    fun openInventory() {
        if (isMenuVisible) {
            screenState = GameScreenState.INVENTORY
        }
    }

    fun openShop() {
        if (isMenuVisible) {
            screenState = GameScreenState.SHOP
        }
    }

    fun startGame(difficulty: GameDifficulty = activeDifficulty) {
        activeDifficulty = difficulty
        restartRun(mode = GameMode.NORMAL)
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    fun openEndlessSetup(difficulty: GameDifficulty = activeDifficulty) {
        if (!isMenuVisible) return
        activeDifficulty = difficulty
        screenState = GameScreenState.ENDLESS_SETUP
    }

    fun selectEndlessZone(zone: GameZone) {
        if (isEndlessSetupVisible) selectedEndlessZone = zone
    }

    fun confirmEndlessSetup() {
        if (isEndlessSetupVisible) startEndless(selectedEndlessZone, activeDifficulty)
    }

    fun startEndless(
        zone: GameZone,
        difficulty: GameDifficulty = activeDifficulty,
    ) {
        activeDifficulty = difficulty
        restartRun(mode = GameMode.ENDLESS, endlessZone = zone)
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    fun continueEndless() {
        if (!canContinueEndless) return
        bankRunCoins()
        engine.continueEndless()
        isPaused = false
    }

    fun returnToMenu() {
        bankRunCoins()
        isPaused = false
        screenState = GameScreenState.MENU
    }

    fun restartGame() {
        if (!isGameVisible || engine.isRunning) return
        restartRun()
        isPaused = false
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
            restartGame()
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

    private fun restartRun(
        mode: GameMode = engine.mode,
        endlessZone: GameZone = engine.snapshot().zone,
    ) {
        bankRunCoins()
        engine.restart(activeDifficulty, mode, endlessZone)
        runNumber++
        bankedRunCoins = 0
    }
}
