package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineBossTest {
    private fun reachBoss(engine: GameEngine) {
        repeat(3) {
            engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            if (it < 2) finishTransition(engine)
        }
    }

    private fun finishTransition(engine: GameEngine) {
        repeat(110) {
            if (!engine.snapshot().isZoneTransitioning) return
            engine.update(0.05)
        }
        assertFalse(engine.snapshot().isZoneTransitioning)
    }

    @Test
    fun thirdZoneEndsWithBossTransitionInsteadOfVictory() {
        val engine = GameEngine(gameAreaWidth = 1280.0)
        assertNull(engine.snapshot().catapult)
        reachBoss(engine)

        val snapshot = engine.snapshot()
        assertEquals(GameZone.BOSS, snapshot.zone)
        assertEquals(GameStatus.RUNNING, snapshot.status)
        assertTrue(snapshot.isZoneTransitioning)
        assertTrue(snapshot.obstacles.isEmpty())
        assertTrue(snapshot.coins.isEmpty())
        assertEquals(1280.0, assertNotNull(snapshot.catapult).x)

        val velocity = snapshot.verticalVelocity
        engine.flap()
        engine.update(0.05)
        assertEquals(snapshot.catapult, engine.snapshot().catapult)
        assertEquals(snapshot.points, engine.snapshot().points)
        assertEquals(velocity, engine.snapshot().verticalVelocity)
    }

    @Test
    fun catapultEntersFromRightAndStopsInsideResizedViewportWithoutObstacles() {
        val engine = GameEngine(gameAreaWidth = 1280.0)
        reachBoss(engine)
        engine.resizeGameArea(-160.0, 1440.0)
        finishTransition(engine)
        assertEquals(1440.0, assertNotNull(engine.snapshot().catapult).x)

        repeat(20) { engine.update(0.05) }
        val halfway = assertNotNull(engine.snapshot().catapult)
        assertTrue(halfway.x < 1440.0)
        assertTrue(halfway.isEntering)

        repeat(30) {
            engine.resizeGameArea(-160.0, 1440.0)
            engine.update(0.05)
            assertTrue(engine.snapshot().obstacles.isEmpty())
            assertTrue(engine.snapshot().coins.isEmpty())
        }
        val settled = assertNotNull(engine.snapshot().catapult)
        assertFalse(settled.isEntering)
        assertEquals(1440.0 - settled.size - GameEngineConfig.CATAPULT_RIGHT_MARGIN, settled.x)
        assertTrue(settled.x >= -160.0)
        assertTrue(settled.x + settled.size < 1440.0)
        engine.update(0.05)
        assertEquals(settled, engine.snapshot().catapult)

        engine.restart()
        assertEquals(GameZone.ZONE_ONE, engine.snapshot().zone)
        assertNull(engine.snapshot().catapult)
        assertTrue(engine.snapshot().obstacles.isNotEmpty())
        reachBoss(engine)
        assertTrue(assertNotNull(engine.snapshot().catapult).isEntering)
        assertEquals(1440.0, assertNotNull(engine.snapshot().catapult).x)
    }

    @Test
    fun pauseAndMenuFreezeBossEntranceAndNewRunResetsIt() {
        val controller = GameController(gameAreaWidth = 1280.0)
        controller.startGame()
        repeat(3) { zone ->
            controller.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            if (zone < 2) {
                while (controller.snapshot().isZoneTransitioning) controller.update(0.05)
            }
        }
        while (controller.snapshot().isZoneTransitioning) controller.update(0.05)
        controller.update(0.05)
        val beforePause = controller.snapshot()
        controller.pauseGame()
        controller.update(10.0)
        assertEquals(beforePause, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertTrue(assertNotNull(controller.snapshot().catapult).x < assertNotNull(beforePause.catapult).x)

        controller.returnToMenu()
        val inMenu = controller.snapshot()
        controller.update(10.0)
        assertEquals(inMenu, controller.snapshot())
        controller.startGame()
        assertEquals(GameZone.ZONE_ONE, controller.snapshot().zone)
        assertNull(controller.snapshot().catapult)
    }

    @Test
    fun bossLastsThirtySecondsAfterThreeNormalZones() {
        assertEquals(GameZone.ZONE_THREE, GameEngineConfig.zoneAt(59.999))
        assertEquals(GameZone.BOSS, GameEngineConfig.zoneAt(60.0))
        assertEquals(GameZone.BOSS, GameEngineConfig.zoneAt(89.999))
        assertEquals(25.0, GameEngineConfig.zoneElapsedAt(85.0))
        assertEquals(90.0, GameEngineConfig.TIME_TO_WIN_SECONDS)
    }
}
