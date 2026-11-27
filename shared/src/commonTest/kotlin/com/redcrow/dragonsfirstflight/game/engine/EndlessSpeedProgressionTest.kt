package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EndlessSpeedProgressionTest {
    @Test
    fun currentSpeedMovesExistingEnemiesAndRewardsOnEveryMapAndDifficulty() {
        for (difficulty in GameDifficulty.entries) {
            for (zone in GameZone.entries) {
                val engine = newEngine(difficulty, zone)
                engine.resizeGameArea(10_000.0, 11_280.0)
                val initial = engine.snapshot()
                engine.update(0.05)
                val first = engine.snapshot()
                assertTrue(first.obstacleSpeed > initial.obstacleSpeed)
                assertEquals(first.obstacleSpeed * 0.05, first.coinScrollX - initial.coinScrollX, TOLERANCE)
                assertEquals(first.coinScrollX, first.extraLifeScrollX, TOLERANCE)
                val enemy = assertNotNull(initial.firstObstacle)
                val moved = assertNotNull(first.obstacles.find { it.id == enemy.id })
                assertEquals(first.obstacleSpeed * 0.05, enemy.x - moved.x, TOLERANCE)
                repeat(40) { engine.update(0.05) }
                assertTrue(engine.snapshot().obstacleSpeed > first.obstacleSpeed)
                assertEquals(zone, engine.snapshot().zone)
            }
        }
    }

    @Test
    fun lateEnemiesUseTheSameAccelerationAndKeepTheirWarningAndRecoverySpacing() {
        for (difficulty in GameDifficulty.entries) {
            val engine = newEngine(difficulty, GameZone.ZONE_THREE)
            engine.resizeGameArea(10_000.0, 11_280.0)
            val initialSpeed = engine.snapshot().obstacleSpeed
            val initialSeparation = difficulty.obstacleSeparationAt(GameEngineConfig.ZONE_DURATION_SECONDS * 2)
            var newEnemies = 0
            var rushWarnings = 0
            repeat(6000) {
                val before = engine.snapshot()
                engine.update(0.05)
                val after = engine.snapshot()
                assertEquals(GameStatus.RUNNING, after.status)
                assertTrue(after.obstacleSpeed >= before.obstacleSpeed)
                assertTrue(after.obstacleSpeed <= difficulty.endless.maximumSpeed)
                val previousById = before.obstacles.associateBy { it.id }
                for (enemy in after.obstacles) {
                    val previous = previousById[enemy.id]
                    if (previous != null) {
                        val movementDelta = (0.05 - previous.rushWarningRemainingSeconds).coerceAtLeast(0.0)
                        val distance = after.obstacleSpeed * enemy.horizontalBehavior.speedMultiplier * movementDelta
                        assertEquals(distance, previous.x - enemy.x, TOLERANCE)
                    } else {
                        newEnemies++
                        val preceding = after.obstacles.lastOrNull { it.id < enemy.id }
                        if (preceding != null) {
                            val minimumGap =
                                initialSeparation * after.obstacleSpeed / initialSpeed *
                                    enemy.horizontalBehavior.separationMultiplier
                            assertTrue(enemy.x - preceding.x + TOLERANCE >= minimumGap)
                        }
                        if (enemy.horizontalBehavior.isRush) {
                            rushWarnings++
                            assertEquals(
                                GameEngineConfig.RUSH_WARNING_DURATION_SECONDS,
                                enemy.rushWarningRemainingSeconds,
                            )
                            assertEquals(ObstacleMovement.STATIC, enemy.movement)
                            assertFalse(preceding?.horizontalBehavior?.isRush == true)
                        }
                    }
                }
            }
            assertTrue(newEnemies > 30)
            if (difficulty != GameDifficulty.EASY) assertTrue(rushWarnings > 0)
            assertEquals(difficulty.endless.maximumSpeed, engine.snapshot().obstacleSpeed)
        }
    }

    @Test
    fun pauseAndMenuFreezeProgressionAndResumeDoesNotCountBackgroundTime() {
        val controller = GameController(1280.0, random = Random(7))
        controller.startEndless(GameZone.ZONE_TWO, GameDifficulty.HARD)
        repeat(20) { controller.update(0.05) }
        val playing = controller.snapshot()
        controller.pauseGame()
        controller.update(120.0)
        assertEquals(playing, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        val resumed = controller.snapshot()
        assertEquals(playing.endlessElapsedSeconds + 0.05, resumed.endlessElapsedSeconds, TOLERANCE)
        assertEquals(playing.obstacleSpeed + 0.05 * 1.2, resumed.obstacleSpeed, TOLERANCE)
        controller.returnToMenu()
        val menu = controller.snapshot()
        controller.update(120.0)
        assertEquals(menu, controller.snapshot())
    }

    @Test
    fun normalPlayAndTheVictoryTransitionDoNotContributeToEndlessTime() {
        val engine = GameEngine(1280.0, Random(7), initialDifficulty = GameDifficulty.HARD)
        engine.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        assertEquals(0.0, engine.snapshot().endlessElapsedSeconds)
        engine.continueEndless()
        assertEquals(390.0, engine.snapshot().obstacleSpeed)
        while (engine.snapshot().isZoneTransitioning) {
            engine.update(0.05)
            assertEquals(0.0, engine.snapshot().endlessElapsedSeconds)
            assertEquals(390.0, engine.snapshot().obstacleSpeed)
        }
        engine.update(0.05)
        assertEquals(0.05, engine.snapshot().endlessElapsedSeconds, TOLERANCE)
        assertEquals(390.06, engine.snapshot().obstacleSpeed, TOLERANCE)
        engine.continueEndless()
        assertEquals(390.06, engine.snapshot().obstacleSpeed, TOLERANCE)
    }

    @Test
    fun aLongFrameCannotCauseAnInstantJumpAndRestartClearsProgression() {
        val engine = newEngine(GameDifficulty.EASY, GameZone.ZONE_TWO)
        engine.update(120.0)
        assertEquals(GameEngineConfig.MAX_DELTA_SECONDS, engine.snapshot().endlessElapsedSeconds)
        assertEquals(195.03, engine.snapshot().obstacleSpeed, TOLERANCE)
        engine.restart()
        assertEquals(0.0, engine.snapshot().endlessElapsedSeconds)
        assertEquals(195.0, engine.snapshot().obstacleSpeed)
        engine.restart(mode = GameMode.NORMAL)
        engine.update(0.05)
        assertEquals(GameDifficulty.EASY.obstacleSpeedAt(0.05), engine.snapshot().obstacleSpeed)
        assertEquals(0.0, engine.snapshot().endlessElapsedSeconds)
    }

    private fun newEngine(
        difficulty: GameDifficulty,
        zone: GameZone,
    ) = GameEngine(
        1280.0,
        Random(7),
        initialDifficulty = difficulty,
        initialMode = GameMode.ENDLESS,
        initialEndlessZone = zone,
    )

    private companion object {
        const val TOLERANCE = 0.000001
    }
}
