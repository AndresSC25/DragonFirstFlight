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
import kotlin.test.assertTrue

class ContinueEndlessTest {
    @Test
    fun victoryContinuationKeepsTheSameRunAndDifficulty() {
        val controller = GameController(1280.0, random = Random(7))
        assertFalse(controller.canContinueEndless)
        controller.startGame(GameDifficulty.HARD)
        controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        assertTrue(controller.canContinueEndless)
        val victory = controller.snapshot()
        val run = controller.runNumber

        controller.continueEndless()
        assertTrue(controller.isGameRunning)
        assertFalse(controller.canContinueEndless)
        assertEquals(run, controller.runNumber)
        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
        assertEquals(victory.points, controller.snapshot().points)
        assertEquals(victory.lives, controller.snapshot().lives)
        assertEquals(GameMode.ENDLESS, controller.snapshot().mode)
        assertEquals(GameZone.BOSS, controller.snapshot().zone)

        controller.update(0.5)
        val playing = controller.snapshot()
        repeat(3) { controller.continueEndless() }
        assertEquals(playing, controller.snapshot())
        assertEquals(run, controller.runNumber)
    }

    @Test
    fun pausingTheEndlessTransitionPreservesTheAnnouncementAndProgress() {
        val controller = GameController(1280.0, random = Random(7))
        controller.startGame()
        controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        controller.continueEndless()
        controller.update(0.5)
        val during = controller.snapshot()
        assertTrue(during.isZoneTransitioning)
        controller.pauseGame()
        controller.update(30.0)
        assertEquals(during, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertTrue(controller.snapshot().zoneTransitionProgress > during.zoneTransitionProgress)
    }

    @Test
    fun continuationIsUnavailableInMenusOrBeforeVictory() {
        val controller = GameController(1280.0, random = Random(7))
        controller.continueEndless()
        assertTrue(controller.isMenuVisible)
        assertEquals(0, controller.runNumber)
        controller.startGame()
        controller.continueEndless()
        assertEquals(GameMode.NORMAL, controller.snapshot().mode)
        controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        controller.returnToMenu()
        val inMenu = controller.snapshot()
        assertFalse(controller.canContinueEndless)
        controller.continueEndless()
        assertEquals(inMenu, controller.snapshot())
        assertTrue(controller.isMenuVisible)
    }

    @Test
    fun coinsCollectedBeforeAndAfterVictoryAreBankedExactlyOnce() {
        var checked = false
        for (seed in 1..60) {
            val storage = InmemoryStorage()
            val controller = GameController(1280.0, CoinWallet(storage), Random(seed))
            controller.startGame()
            var frame = 0
            while (controller.isGameRunning && controller.snapshot().runCoins == 0 && frame < 1200) {
                if (frame % 6 == 0) controller.handleGameAreaInput()
                controller.update(0.05)
                frame++
            }
            if (!controller.isGameRunning || controller.snapshot().runCoins == 0) continue
            controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
            assertEquals(GameStatus.WON, controller.snapshot().status)
            val earnedAtVictory = controller.snapshot().runCoins
            val balanceAtVictory = controller.coinBalance
            assertEquals(earnedAtVictory.toString(), storage.getOrNull("coins.balance"))

            controller.continueEndless()
            repeat(3) { controller.continueEndless() }
            assertEquals(balanceAtVictory, controller.coinBalance)
            assertEquals(earnedAtVictory, controller.snapshot().runCoins)
            assertEquals(earnedAtVictory.toString(), storage.getOrNull("coins.balance"))
            frame = 0
            while (controller.isGameRunning && controller.snapshot().runCoins == earnedAtVictory && frame < 1200) {
                if (frame % 6 == 0) controller.handleGameAreaInput()
                controller.update(0.05)
                frame++
            }
            if (controller.snapshot().runCoins == earnedAtVictory) continue
            val totalEarned = controller.snapshot().runCoins
            controller.returnToMenu()
            controller.returnToMenu()
            assertEquals(totalEarned.toString(), storage.getOrNull("coins.balance"))
            assertEquals(totalEarned, controller.coinBalance)
            controller.startGame()
            assertEquals(totalEarned, controller.coinBalance)
            checked = true
            break
        }
        assertTrue(checked, "A seeded continuation must collect new coins after its victory reward was banked")
    }
}
