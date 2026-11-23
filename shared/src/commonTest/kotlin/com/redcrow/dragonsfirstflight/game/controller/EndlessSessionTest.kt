package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EndlessSessionTest {
    @Test
    fun retryKeepsEveryMapAndDifficultyButResetsTheEndlessRun() {
        for (zone in GameZone.entries) {
            for (difficulty in GameDifficulty.entries) {
                val controller = GameController(240.0, random = Random(7))
                controller.startEndless(zone, difficulty)
                reachDefeat(controller)
                val run = controller.runNumber
                controller.restartGame()
                val fresh = controller.snapshot()
                assertEquals(run + 1, controller.runNumber)
                assertEquals(GameMode.ENDLESS, fresh.mode)
                assertEquals(zone, fresh.zone)
                assertEquals(difficulty, controller.activeDifficulty)
                assertEquals(0, fresh.points)
                assertEquals(0, fresh.runCoins)
                assertEquals(0.0, fresh.endlessElapsedSeconds)
                assertEquals(difficulty.endlessStartingSpeed(zone), fresh.obstacleSpeed)
                assertEquals(GameEngineConfig.INITIAL_LIVES, fresh.lives)
                assertFalse(fresh.isInvulnerable)
                assertFalse(fresh.isImpactFeedbackActive)
                assertNull(fresh.catapult)
                assertTrue(fresh.projectiles.isEmpty())
                controller.restartGame()
                assertEquals(fresh, controller.snapshot())
                assertEquals(run + 1, controller.runNumber)
            }
        }
    }

    @Test
    fun retryAfterVictoryContinuationStartsDirectlyInEndlessOnTheBattlefield() {
        val controller = GameController(1280.0, random = Random(7))
        controller.startGame(GameDifficulty.HARD)
        controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        controller.continueEndless()
        controller.resizeGameArea(0.0, 240.0)
        reachDefeat(controller)
        controller.restartGame()
        assertTrue(controller.isGameRunning)
        assertEquals(GameMode.ENDLESS, controller.snapshot().mode)
        assertEquals(GameZone.BOSS, controller.snapshot().zone)
        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
        assertEquals(0, controller.snapshot().points)
        assertFalse(controller.snapshot().isZoneTransitioning)
        assertNull(controller.snapshot().catapult)
    }

    @Test
    fun retryCannotStartAGameFromTheMenuOrSetup() {
        val controller = GameController(1280.0)
        controller.restartGame()
        assertTrue(controller.isMenuVisible)
        controller.openEndlessSetup()
        controller.restartGame()
        assertTrue(controller.isEndlessSetupVisible)
        assertEquals(0, controller.runNumber)
    }

    @Test
    fun leavingEndlessBanksCoinsOnceAndPlayStartsTheNormalRoute() {
        var checked = false
        for (seed in 1..30) {
            val storage = InmemoryStorage()
            val controller = GameController(1280.0, CoinWallet(storage), Random(seed))
            controller.startEndless(GameZone.ZONE_ONE)
            var frame = 0
            while (controller.isGameRunning && controller.snapshot().runCoins == 0 && frame < 1200) {
                if (frame % 6 == 0) controller.handleGameAreaInput()
                controller.update(0.05)
                frame++
            }
            val earned = controller.snapshot().runCoins
            if (earned == 0) continue
            controller.pauseGame()
            controller.pauseGame()
            controller.returnToMenu()
            controller.returnToMenu()
            assertEquals(earned, controller.coinBalance)
            assertEquals(earned.toString(), storage.getOrNull("coins.balance"))
            controller.startGame(GameDifficulty.MEDIUM)
            assertEquals(GameMode.NORMAL, controller.snapshot().mode)
            assertEquals(GameZone.ZONE_ONE, controller.snapshot().zone)
            assertEquals(0, controller.snapshot().runCoins)
            assertEquals(earned, controller.coinBalance)
            checked = true
            break
        }
        assertTrue(checked, "A seeded Endless run must collect coins for the banking check")
    }

    private fun reachDefeat(controller: GameController) {
        var frame = 0
        while (controller.isGameRunning && frame < 3000) {
            controller.update(0.05)
            frame++
        }
        assertEquals(GameStatus.LOST, controller.snapshot().status, "The seeded run must reach defeat")
    }
}
