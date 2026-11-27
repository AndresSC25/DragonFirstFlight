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

/**
 * Owns the active screen, pause state and run lifecycle, and drives the game engine.
 * Coins earned in a run are deposited into the wallet when the run ends, pauses or the player leaves.
 */
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

    /** Counts runs started since creation; changes on every restart. */
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

    /** Wallet balance plus coins collected in the current run that are not yet deposited. */
    val coinBalance: Int
        get() = coinWallet.balance + (engine.runCoins - bankedRunCoins)

    /** Opens the inventory; only works from the menu. */
    fun openInventory() {
        if (isMenuVisible) {
            screenState = GameScreenState.INVENTORY
        }
    }

    /** Opens the shop; only works from the menu. */
    fun openShop() {
        if (isMenuVisible) {
            screenState = GameScreenState.SHOP
        }
    }

    /** Starts a normal run at [difficulty] and shows the game screen. */
    fun startGame(difficulty: GameDifficulty = activeDifficulty) {
        activeDifficulty = difficulty
        restartRun(mode = GameMode.NORMAL)
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    /** Opens endless setup with [difficulty]; only works from the menu. */
    fun openEndlessSetup(difficulty: GameDifficulty = activeDifficulty) {
        if (!isMenuVisible) return
        activeDifficulty = difficulty
        screenState = GameScreenState.ENDLESS_SETUP
    }

    /** Picks the starting zone for endless mode; only works on the setup screen. */
    fun selectEndlessZone(zone: GameZone) {
        if (isEndlessSetupVisible) selectedEndlessZone = zone
    }

    /** Starts endless mode with the selected zone and difficulty; only works on the setup screen. */
    fun confirmEndlessSetup() {
        if (isEndlessSetupVisible) startEndless(selectedEndlessZone, activeDifficulty)
    }

    /** Starts an endless run in [zone] at [difficulty] and shows the game screen. */
    fun startEndless(
        zone: GameZone,
        difficulty: GameDifficulty = activeDifficulty,
    ) {
        activeDifficulty = difficulty
        restartRun(mode = GameMode.ENDLESS, endlessZone = zone)
        isPaused = false
        screenState = GameScreenState.PLAYING
    }

    /** Turns a won normal run into endless play; does nothing unless [canContinueEndless]. */
    fun continueEndless() {
        if (!canContinueEndless) return
        bankRunCoins()
        engine.continueEndless()
        isPaused = false
    }

    /** Deposits run coins, clears pause and shows the menu. */
    fun returnToMenu() {
        bankRunCoins()
        isPaused = false
        screenState = GameScreenState.MENU
    }

    /** Restarts the current mode once the run has ended; does nothing while it is running. */
    fun restartGame() {
        if (!isGameVisible || engine.isRunning) return
        restartRun()
        isPaused = false
    }

    /** Pauses a running game and deposits its coins; does nothing otherwise. */
    fun pauseGame() {
        if (isGameVisible && engine.isRunning) {
            isPaused = true
            bankRunCoins()
        }
    }

    /** Resumes a paused running game; does nothing otherwise. */
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

    /** Flaps while the run is active, or restarts it after it ended; ignored when paused or not playing. */
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

    /** Advances the engine by [elapsedSeconds]; ignored when paused or not playing. Deposits coins at run end. */
    fun update(elapsedSeconds: Double) {
        if (!isGameVisible || isPaused) {
            return
        }

        engine.update(elapsedSeconds)

        if (!engine.isRunning) {
            bankRunCoins()
        }
    }

    /** Updates the horizontal play area bounds in game-area pixels. */
    fun resizeGameArea(
        leftBoundary: Double,
        rightBoundary: Double,
    ) {
        engine.resizeGameArea(
            leftBoundary = leftBoundary,
            rightBoundary = rightBoundary,
        )
    }

    /** Returns the current engine state for rendering and audio cues. */
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
