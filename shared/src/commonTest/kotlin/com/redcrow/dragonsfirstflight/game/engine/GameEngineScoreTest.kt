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
    fun passingMoreThanFiveObstaclesDoesNotEndTheGame() {
        val engine = GameEngine(gameAreaWidth = 1280.0)
        var passedObstacles = 0
        var previousRightEdge = engine.snapshot().obstacleX + GameEngineConfig.OBSTACLE_SIZE

        repeat(1400) {
            engine.update(0.05)
            val rightEdge = engine.snapshot().obstacleX + GameEngineConfig.OBSTACLE_SIZE
            if (previousRightEdge > GameEngineConfig.DRAGON_X &&
                rightEdge <= GameEngineConfig.DRAGON_X
            ) {
                passedObstacles += 1
            }
            previousRightEdge = rightEdge
        }

        assertTrue(passedObstacles > 5)
        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        assertEquals(GameEngineConfig.INITIAL_LIVES, engine.snapshot().lives)
        assertEquals(70 * GameEngineConfig.POINTS_PER_SECOND, engine.snapshot().points)
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
