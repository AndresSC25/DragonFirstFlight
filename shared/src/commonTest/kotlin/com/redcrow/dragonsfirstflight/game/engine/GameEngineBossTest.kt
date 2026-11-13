package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineBossTest {
    @Test
    fun furyStartsOnlyInLastTenSecondsFreezesOnPauseAndClearsOnVictoryOrRestart() {
        val controller = GameController(gameAreaWidth = 1280.0)
        controller.startGame()
        repeat(3) {
            controller.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            repeat(110) {
                if (controller.snapshot().isZoneTransitioning) controller.update(0.05)
            }
        }
        controller.update(GameEngineConfig.BOSS_FURY_START_SECONDS - 0.1)
        assertFalse(assertNotNull(controller.snapshot().catapult).isFurious)
        controller.update(0.1)
        val start = assertNotNull(controller.snapshot().catapult)
        assertTrue(start.isFurious)
        assertEquals(0.0, start.furyPulse, 0.000001)
        controller.update(0.3)
        val furious = controller.snapshot()
        val pulse = assertNotNull(furious.catapult).furyPulse
        assertTrue(pulse > 0.0 && pulse <= 1.0)
        controller.pauseGame()
        controller.update(10.0)
        assertEquals(furious, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertTrue(assertNotNull(controller.snapshot().catapult).furyPulse > pulse)
        controller.update(GameEngineConfig.BOSS_FURY_DURATION_SECONDS - 0.3)
        assertEquals(GameStatus.WON, controller.snapshot().status)
        assertFalse(assertNotNull(controller.snapshot().catapult).isFurious)
        assertEquals(0.0, assertNotNull(controller.snapshot().catapult).furyPulse)
        controller.startGame()
        assertNull(controller.snapshot().catapult)
    }

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
    fun firstShotFollowsEntryAndWarningAndMovesWithoutDamagingDragon() {
        val engine = GameEngine(gameAreaWidth = 1280.0)
        reachBoss(engine)
        finishTransition(engine)
        var sawWarning = false
        var firstShot: Projectile? = null
        val startingLives = engine.snapshot().lives
        repeat(200) {
            if (firstShot != null) return@repeat
            engine.update(0.05)
            val snapshot = engine.snapshot()
            val catapult = assertNotNull(snapshot.catapult)
            if (catapult.isEntering) {
                assertNull(catapult.warningFrameIndex)
                assertTrue(snapshot.projectiles.isEmpty())
            }
            if (catapult.warningFrameIndex != null) sawWarning = true
            firstShot = snapshot.projectiles.firstOrNull()
        }
        assertTrue(sawWarning)
        val shot = assertNotNull(firstShot)
        val catapult = assertNotNull(engine.snapshot().catapult)
        assertEquals(5, catapult.frameIndex)
        assertNull(catapult.warningFrameIndex)
        assertEquals(
            catapult.x + catapult.size * GameEngineConfig.CATAPULT_RELEASE_X_FRACTION - shot.size / 2,
            shot.x,
        )
        assertEquals(0.0, shot.ageSeconds)

        engine.update(0.05)
        assertEquals(1, engine.snapshot().projectiles.size)
        val movedShot = engine.snapshot().projectiles.single()
        assertEquals(shot.x + shot.velocityX * 0.05, movedShot.x)
        repeat(65) {
            if (shot.targetY + shot.size / 2.0 >= 400.0) engine.flap()
            engine.update(0.05)
        }
        assertTrue(engine.snapshot().projectiles.none { it.id == shot.id })
        assertEquals(startingLives, engine.snapshot().lives)
        assertTrue(engine.snapshot().obstacles.isEmpty())
        engine.restart()
        assertTrue(engine.snapshot().projectiles.isEmpty())
    }

    @Test
    fun victoryClearsProjectilesAndWarnings() {
        val engine = GameEngine(gameAreaWidth = 1280.0)
        reachBoss(engine)
        finishTransition(engine)
        repeat(120) { engine.update(0.05) }
        assertTrue(engine.snapshot().projectiles.isNotEmpty())
        engine.update(GameEngineConfig.BOSS_DURATION_SECONDS)
        assertEquals(GameStatus.WON, engine.snapshot().status)
        assertTrue(engine.snapshot().projectiles.isEmpty())
        assertNull(assertNotNull(engine.snapshot().catapult).warningFrameIndex)
    }

    @Test
    fun pauseFreezesAttackAndProjectileUntilResume() {
        val controller = GameController(gameAreaWidth = 1280.0)
        controller.startGame()
        repeat(3) { zone ->
            controller.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            if (zone < 2) {
                while (controller.snapshot().isZoneTransitioning) controller.update(0.05)
            }
        }
        while (controller.snapshot().isZoneTransitioning) controller.update(0.05)
        repeat(120) { controller.update(0.05) }
        val beforePause = controller.snapshot()
        assertTrue(beforePause.projectiles.isNotEmpty())
        controller.pauseGame()
        controller.update(10.0)
        assertEquals(beforePause, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        val afterResume = controller.snapshot()
        assertTrue(afterResume.projectiles.first().x < beforePause.projectiles.first().x)
        controller.startGame()
        assertTrue(controller.snapshot().projectiles.isEmpty())
        assertNull(controller.snapshot().catapult)
    }

    @Test
    fun bossLastsSixtySecondsWithThreeTwentySecondStages() {
        assertEquals(GameZone.ZONE_THREE, GameEngineConfig.zoneAt(59.999))
        assertEquals(GameZone.BOSS, GameEngineConfig.zoneAt(60.0))
        assertEquals(GameZone.BOSS, GameEngineConfig.zoneAt(119.999))
        assertEquals(25.0, GameEngineConfig.zoneElapsedAt(85.0))
        assertEquals(120.0, GameEngineConfig.TIME_TO_WIN_SECONDS)
        assertEquals(BossAttackStage.SINGLE, BossPatternPlanner.stageAt(19.999))
        assertEquals(BossAttackStage.DOUBLE, BossPatternPlanner.stageAt(20.0))
        assertEquals(BossAttackStage.DOUBLE, BossPatternPlanner.stageAt(39.999))
        assertEquals(BossAttackStage.CORRIDOR, BossPatternPlanner.stageAt(40.0))
        assertEquals(BossAttackStage.CORRIDOR, BossPatternPlanner.stageAt(59.999))
        assertEquals(50.0, GameEngineConfig.BOSS_FURY_START_SECONDS)

        val engine = GameEngine(gameAreaWidth = 1280.0)
        reachBoss(engine)
        finishTransition(engine)
        engine.update(35.0)
        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        engine.update(24.999)
        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        engine.update(0.001)
        assertEquals(GameStatus.WON, engine.snapshot().status)
    }
}
