package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineContinueEndlessTest {
    @Test
    fun victoryContinuesOnTheBattlefieldWithTheRemainingLivesAndScore() {
        for (difficulty in GameDifficulty.entries) {
            val engine = GameEngine(1280.0, Random(7), initialDifficulty = difficulty)
            engine.handleDamageEvent(DamageEvent)
            repeat(3) {
                engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
                while (engine.snapshot().isZoneTransitioning) engine.update(0.05)
            }
            repeat(120) { engine.update(0.05) }
            assertTrue(engine.snapshot().projectiles.isNotEmpty())
            engine.update(GameEngineConfig.BOSS_DURATION_SECONDS)
            val victory = engine.snapshot()
            assertEquals(GameStatus.WON, victory.status)
            assertTrue(victory.lives in 1 until GameEngineConfig.INITIAL_LIVES)

            engine.continueEndless()
            val continued = engine.snapshot()
            assertEquals(GameMode.ENDLESS, continued.mode)
            assertEquals(GameZone.BOSS, continued.zone)
            assertEquals(GameStatus.RUNNING, continued.status)
            assertEquals(victory.lives, continued.lives)
            assertEquals(victory.points, continued.points)
            assertEquals(victory.runCoins, continued.runCoins)
            assertNull(continued.catapult)
            assertTrue(continued.projectiles.isEmpty())
            assertTrue(continued.rockImpacts.isEmpty())
            assertTrue(continued.isZoneTransitioning)
            assertEquals(0.0, continued.zoneTransitionProgress)

            engine.resizeGameArea(10_000.0, 11_280.0)
            assertTrue(engine.snapshot().obstacles.isEmpty())
            while (engine.snapshot().isZoneTransitioning) engine.update(0.05)
            engine.resizeGameArea(10_000.0, 11_280.0)
            val enemy = assertNotNull(engine.snapshot().firstObstacle)
            engine.update(0.05)
            val moved = assertNotNull(engine.snapshot().firstObstacle)
            assertEquals(
                (
                    difficulty.endlessStartingSpeed(GameZone.BOSS, afterVictory = true) +
                        difficulty.endless.speedIncreasePerSecond * 0.05
                ) * 0.05,
                enemy.x - moved.x,
                0.000001,
            )
            repeat(2400) { engine.update(0.05) }
            val later = engine.snapshot()
            assertEquals(GameStatus.RUNNING, later.status)
            assertEquals(GameZone.BOSS, later.zone)
            assertTrue(later.points > victory.points)
            assertTrue(later.obstacles.isNotEmpty())
            assertTrue(later.obstacles.all { "/zone3/" in it.spritePath })
            assertNull(later.catapult)
            assertTrue(later.projectiles.isEmpty())
        }
    }

    @Test
    fun theEndlessTransitionFreezesFlightAndDelaysEnemiesUntilTheCardFinishes() {
        val engine = GameEngine(1280.0, Random(7))
        engine.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        engine.continueEndless()
        val start = engine.snapshot()
        engine.flap()
        repeat(10) {
            engine.resizeGameArea(0.0, 1280.0)
            engine.update(0.05)
        }
        val during = engine.snapshot()
        assertTrue(during.isZoneTransitioning)
        assertTrue(during.zoneTransitionProgress > start.zoneTransitionProgress)
        assertEquals(start.points, during.points)
        assertEquals(start.lives, during.lives)
        assertEquals(start.runCoins, during.runCoins)
        assertEquals(start.dragonY, during.dragonY)
        assertEquals(start.verticalVelocity, during.verticalVelocity)
        assertTrue(during.obstacles.isEmpty())
        assertTrue(during.projectiles.isEmpty())
        assertNull(during.catapult)
        repeat(40) {
            if (engine.snapshot().isZoneTransitioning) engine.update(0.05)
        }
        assertFalse(engine.snapshot().isZoneTransitioning)
        engine.update(0.05)
        assertTrue(engine.snapshot().obstacles.isNotEmpty())
    }

    @Test
    fun onlyANormalVictoryCanBeContinued() {
        val engine = GameEngine(1280.0, Random(7))
        val running = engine.snapshot()
        engine.continueEndless()
        assertEquals(running, engine.snapshot())

        engine.resizeGameArea(10_000.0, 11_280.0)
        repeat(GameEngineConfig.INITIAL_LIVES) {
            engine.handleDamageEvent(DamageEvent)
            if (engine.isRunning) repeat(31) { engine.update(0.05) }
        }
        val lost = engine.snapshot()
        assertEquals(GameStatus.LOST, lost.status)
        engine.continueEndless()
        assertEquals(lost, engine.snapshot())

        engine.restart(mode = GameMode.ENDLESS, endlessZone = GameZone.ZONE_TWO)
        engine.update(0.5)
        val endless = engine.snapshot()
        engine.continueEndless()
        assertEquals(endless, engine.snapshot())
    }

    @Test
    fun anotherContinueRequestDoesNotResetTheOngoingFlight() {
        val engine = GameEngine(1280.0, Random(7))
        engine.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        engine.continueEndless()
        engine.update(0.5)
        val playing = engine.snapshot()
        engine.continueEndless()
        assertEquals(playing, engine.snapshot())
    }
}
