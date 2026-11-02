package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameEngineScoreTest {
    @Test
    fun obstacleSpeedIncreasesWithActiveTimeAndStopsAtConfiguredMaximum() {
        assertEquals(
            GameEngineConfig.OBSTACLE_SPEED,
            GameEngineConfig.obstacleSpeedAt(0.0),
        )
        assertEquals(
            (
                GameEngineConfig.OBSTACLE_SPEED +
                    GameEngineConfig.MAX_OBSTACLE_SPEED
            ) / 2.0,
            GameEngineConfig.obstacleSpeedAt(
                GameEngineConfig.TIME_TO_WIN_SECONDS / 2.0,
            ),
        )
        assertEquals(
            GameEngineConfig.MAX_OBSTACLE_SPEED,
            GameEngineConfig.obstacleSpeedAt(
                GameEngineConfig.TIME_TO_WIN_SECONDS,
            ),
        )
        assertEquals(
            GameEngineConfig.MAX_OBSTACLE_SPEED,
            GameEngineConfig.obstacleSpeedAt(
                GameEngineConfig.TIME_TO_WIN_SECONDS * 2.0,
            ),
        )
    }

    @Test
    fun obstacleSpeedRejectsInvalidActiveTime() {
        assertFailsWith<IllegalArgumentException> {
            GameEngineConfig.obstacleSpeedAt(-1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            GameEngineConfig.obstacleSpeedAt(Double.NaN)
        }
    }

    @Test
    fun scoreUsesElapsedTimeWithoutAnAutomaticVictory() {
        val engine = GameEngine(gameAreaWidth = 1280.0)

        engine.update(6.0)

        assertEquals(6 * GameEngineConfig.POINTS_PER_SECOND, engine.snapshot().points)
        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
    }

    @Test
    fun reachingConfiguredActiveTimeEndsTheGameWithVictory() {
        val engine = GameEngine(gameAreaWidth = 1280.0)

        engine.update(GameEngineConfig.TIME_TO_WIN_SECONDS)

        assertEquals(GameStatus.WON, engine.snapshot().status)
        assertEquals(
            (
                GameEngineConfig.TIME_TO_WIN_SECONDS *
                    GameEngineConfig.POINTS_PER_SECOND
            ).toInt(),
            engine.snapshot().points,
        )
    }

    @Test
    fun passingAnObstacleDoesNotAwardAdditionalPoints() {
        val engine = GameEngine(gameAreaWidth = 1280.0)

        repeat(187) {
            if (it % 6 == 0) {
                engine.flap()
            }
            engine.update(0.05)
        }

        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        assertEquals(9 * GameEngineConfig.POINTS_PER_SECOND, engine.snapshot().points)
    }
}
