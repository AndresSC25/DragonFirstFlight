package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineScoreTest {
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
            (GameEngineConfig.TIME_TO_WIN_SECONDS *
                GameEngineConfig.POINTS_PER_SECOND).toInt(),
            engine.snapshot().points
        )
    }

    @Test
    fun passingAnObstacleDoesNotAwardAdditionalPoints() {
        val engine = GameEngine(gameAreaWidth = 1280.0)

        repeat(187) { engine.update(0.05) }

        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        assertEquals(GameEngineConfig.INITIAL_LIVES, engine.snapshot().lives)
        assertEquals(9 * GameEngineConfig.POINTS_PER_SECOND, engine.snapshot().points)
        assertTrue(
            engine.snapshot().obstacleX + GameEngineConfig.OBSTACLE_SIZE <=
                GameEngineConfig.DRAGON_X
        )
    }
}
