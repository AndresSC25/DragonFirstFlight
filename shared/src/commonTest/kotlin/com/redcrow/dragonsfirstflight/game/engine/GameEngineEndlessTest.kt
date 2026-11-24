package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineEndlessTest {
    @Test
    fun everyMapAndDifficultyKeepsGeneratingEnemiesBeyondNormalVictoryTime() {
        for (zone in GameZone.entries) {
            for (difficulty in GameDifficulty.entries) {
                val engine = createEndless(zone, difficulty)
                val initialId = assertNotNull(engine.snapshot().firstObstacle).id
                val expectedSpriteZone = if (zone == GameZone.BOSS) 3 else zone.ordinal + 1

                // Keep the enemy field away from the dragon to test duration without disabling damage.
                engine.resizeGameArea(10_000.0, 11_280.0)
                repeat((GameEngineConfig.TIME_TO_WIN_SECONDS * 3).toInt()) { second ->
                    repeat(20) { engine.update(0.05) }
                    val snapshot = engine.snapshot()
                    assertEquals(GameMode.ENDLESS, snapshot.mode)
                    assertEquals(GameStatus.RUNNING, snapshot.status, "$zone/$difficulty at $second seconds")
                    assertEquals(zone, snapshot.zone)
                    assertFalse(snapshot.isZoneTransitioning)
                    assertNull(snapshot.catapult)
                    assertTrue(snapshot.projectiles.isEmpty())
                    assertTrue(snapshot.obstacles.isNotEmpty())
                    assertTrue(snapshot.obstacles.all { "/zone$expectedSpriteZone/" in it.spritePath })
                }

                val before = engine.snapshot()
                assertEquals((GameEngineConfig.TIME_TO_WIN_SECONDS * 3).toInt(), before.points)
                val latest = before.obstacles.last { !it.horizontalBehavior.isRush }
                assertTrue(latest.id > initialId + 20, "Enemies must be replaced throughout a long run")
                engine.update(0.05)
                val moved = assertNotNull(engine.snapshot().obstacles.find { it.id == latest.id })
                assertTrue(moved.x < latest.x, "Enemies must still move after the normal victory threshold")
            }
        }
    }

    @Test
    fun losingAllLivesEndsEndlessOnEveryMap() {
        for (zone in GameZone.entries) {
            val engine = createEndless(zone)
            engine.resizeGameArea(10_000.0, 11_280.0)
            repeat(GameEngineConfig.INITIAL_LIVES) {
                engine.handleDamageEvent(DamageEvent)
                if (engine.isRunning) {
                    repeat((GameEngineConfig.INVULNERABILITY_DURATION_SECONDS / 0.05).toInt() + 1) {
                        engine.update(0.05)
                    }
                }
            }
            assertEquals(GameStatus.LOST, engine.status)
            assertEquals(0, engine.snapshot().lives)
            val finalPoints = engine.snapshot().points
            engine.update(5.0)
            assertEquals(finalPoints, engine.snapshot().points)
        }
    }

    @Test
    fun restartPreservesEndlessMapAndResetsRunState() {
        val engine = createEndless(GameZone.BOSS, GameDifficulty.HARD)
        engine.update(5.0)
        engine.handleDamageEvent(DamageEvent)
        engine.restart()

        val snapshot = engine.snapshot()
        assertEquals(GameMode.ENDLESS, snapshot.mode)
        assertEquals(GameZone.BOSS, snapshot.zone)
        assertEquals(GameEngineConfig.INITIAL_LIVES, snapshot.lives)
        assertEquals(0, snapshot.points)
        assertEquals(0, snapshot.runCoins)
        assertFalse(snapshot.isInvulnerable)
        assertFalse(snapshot.isImpactFeedbackActive)
        assertNull(snapshot.catapult)
        assertTrue(snapshot.projectiles.isEmpty())
        assertNotNull(snapshot.firstObstacle)
    }

    @Test
    fun switchingBackToNormalRestoresZoneTransitionsBossAndVictory() {
        val engine = createEndless(GameZone.BOSS)
        engine.restart(mode = GameMode.NORMAL)
        assertEquals(GameMode.NORMAL, engine.snapshot().mode)
        assertEquals(GameZone.ZONE_ONE, engine.snapshot().zone)
        repeat(3) { zone ->
            engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            assertTrue(engine.snapshot().isZoneTransitioning)
            if (zone < 2) finishTransition(engine)
        }
        assertEquals(GameZone.BOSS, engine.snapshot().zone)
        assertNotNull(engine.snapshot().catapult)
        assertTrue(engine.snapshot().obstacles.isEmpty())
        finishTransition(engine)
        engine.update(GameEngineConfig.BOSS_DURATION_SECONDS)
        assertEquals(GameStatus.WON, engine.status)
    }

    @Test
    fun newEndlessRunClearsAnExistingBossEncounter() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(7))
        repeat(3) {
            engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            finishTransition(engine)
        }
        repeat(120) { engine.update(0.05) }
        assertNotNull(engine.snapshot().catapult)
        assertTrue(engine.snapshot().projectiles.isNotEmpty())

        engine.restart(mode = GameMode.ENDLESS, endlessZone = GameZone.BOSS)
        val snapshot = engine.snapshot()
        assertEquals(GameZone.BOSS, snapshot.zone)
        assertNull(snapshot.catapult)
        assertTrue(snapshot.projectiles.isEmpty())
        assertTrue(snapshot.rockImpacts.isEmpty())
        assertTrue(snapshot.obstacles.isNotEmpty())
        engine.resizeGameArea(-160.0, 1440.0)
        engine.update(0.05)
        assertNull(engine.snapshot().catapult)
        assertTrue(engine.snapshot().obstacles.isNotEmpty())
    }

    private fun createEndless(
        zone: GameZone,
        difficulty: GameDifficulty = GameDifficulty.EASY,
    ) = GameEngine(
        gameAreaWidth = 1280.0,
        random = Random(7),
        initialDifficulty = difficulty,
        initialMode = GameMode.ENDLESS,
        initialEndlessZone = zone,
    )

    private fun finishTransition(engine: GameEngine) {
        repeat(50) {
            if (engine.snapshot().isZoneTransitioning) engine.update(0.05)
        }
        assertFalse(engine.snapshot().isZoneTransitioning)
    }
}
