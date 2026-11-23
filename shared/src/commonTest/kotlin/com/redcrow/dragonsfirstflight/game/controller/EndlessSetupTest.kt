package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EndlessSetupTest {
    @Test
    fun choosingAndCancellingAMapDoesNotStartOrAdvanceARun() {
        val controller = GameController(1280.0, random = Random(7))
        val initialSnapshot = controller.snapshot()
        controller.openEndlessSetup(GameDifficulty.HARD)
        assertTrue(controller.isEndlessSetupVisible)
        assertFalse(controller.isMenuVisible)
        assertFalse(controller.isGameRunning)
        controller.selectEndlessZone(GameZone.BOSS)
        controller.update(300.0)
        assertEquals(initialSnapshot, controller.snapshot())
        assertEquals(0, controller.runNumber)

        controller.returnToMenu()
        assertTrue(controller.isMenuVisible)
        assertFalse(controller.isEndlessSetupVisible)
        controller.confirmEndlessSetup()
        assertEquals(0, controller.runNumber)
        controller.openEndlessSetup(GameDifficulty.EASY)
        assertEquals(GameZone.BOSS, controller.selectedEndlessZone)
        assertEquals(GameDifficulty.EASY, controller.activeDifficulty)
    }

    @Test
    fun confirmationStartsEveryMapWithTheDifficultyChosenInTheMenu() {
        for (zone in GameZone.entries) {
            for (difficulty in GameDifficulty.entries) {
                val controller = GameController(1280.0, random = Random(7))
                controller.openEndlessSetup(difficulty)
                controller.selectEndlessZone(zone)
                controller.confirmEndlessSetup()
                assertFalse(controller.isEndlessSetupVisible)
                assertTrue(controller.isGameRunning)
                val initial = controller.snapshot()
                assertEquals(GameMode.ENDLESS, initial.mode)
                assertEquals(zone, initial.zone)
                assertNull(initial.catapult)
                assertEquals(difficulty, controller.activeDifficulty)
                val obstacle = assertNotNull(initial.firstObstacle)
                controller.update(0.05)
                val moved = assertNotNull(controller.snapshot().firstObstacle)
                val enemyZone = if (zone == GameZone.BOSS) GameZone.ZONE_THREE else zone
                val profileTime = enemyZone.ordinal * GameEngineConfig.ZONE_DURATION_SECONDS
                assertEquals(difficulty.obstacleSpeedAt(profileTime) * 0.05, obstacle.x - moved.x, 0.000001)
            }
        }
    }

    @Test
    fun repeatedConfirmationOrSetupInputDuringPlayCannotRestartTheRun() {
        val controller = GameController(1280.0, random = Random(7))
        controller.openEndlessSetup(GameDifficulty.MEDIUM)
        controller.selectEndlessZone(GameZone.ZONE_TWO)
        controller.confirmEndlessSetup()
        controller.update(0.5)
        val playing = controller.snapshot()
        controller.confirmEndlessSetup()
        controller.openEndlessSetup(GameDifficulty.HARD)
        controller.selectEndlessZone(GameZone.BOSS)
        assertEquals(1, controller.runNumber)
        assertEquals(playing, controller.snapshot())
        assertEquals(GameDifficulty.MEDIUM, controller.activeDifficulty)
        assertEquals(GameZone.ZONE_TWO, controller.selectedEndlessZone)
    }

    @Test
    fun playAfterCancellingSetupStillStartsTheNormalRoute() {
        val controller = GameController(1280.0, random = Random(7))
        controller.openEndlessSetup(GameDifficulty.HARD)
        controller.selectEndlessZone(GameZone.BOSS)
        controller.returnToMenu()
        controller.startGame(GameDifficulty.MEDIUM)
        assertEquals(GameMode.NORMAL, controller.snapshot().mode)
        assertEquals(GameZone.ZONE_ONE, controller.snapshot().zone)
        assertEquals(GameDifficulty.MEDIUM, controller.activeDifficulty)
    }
}
