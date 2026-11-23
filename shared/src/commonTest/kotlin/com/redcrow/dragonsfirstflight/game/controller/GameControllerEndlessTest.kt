package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameControllerEndlessTest {
    @Test
    fun endlessStartsWithTheSelectedMapAndDifficulty() {
        val controller = GameController(1280.0, random = Random(7))
        controller.startEndless(GameZone.BOSS, GameDifficulty.HARD)
        assertTrue(controller.isGameRunning)
        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
        assertEquals(GameMode.ENDLESS, controller.snapshot().mode)
        assertEquals(GameZone.BOSS, controller.snapshot().zone)
    }

    @Test
    fun pauseAndMenuStopEndlessUpdatesAndPlayStartsNormalAgain() {
        val controller = GameController(1280.0, random = Random(7))
        controller.startEndless(GameZone.ZONE_TWO, GameDifficulty.MEDIUM)
        controller.update(0.5)
        val playing = controller.snapshot()
        controller.pauseGame()
        controller.update(300.0)
        assertEquals(playing, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertFalse(playing == controller.snapshot())
        controller.returnToMenu()
        val inMenu = controller.snapshot()
        controller.update(300.0)
        assertEquals(inMenu, controller.snapshot())

        controller.startGame()
        assertEquals(GameMode.NORMAL, controller.snapshot().mode)
        assertEquals(GameZone.ZONE_ONE, controller.snapshot().zone)
        assertEquals(GameDifficulty.MEDIUM, controller.activeDifficulty)
        assertEquals(GameStatus.RUNNING, controller.snapshot().status)
    }
}
