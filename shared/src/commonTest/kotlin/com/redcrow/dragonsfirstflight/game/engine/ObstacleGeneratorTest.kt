package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObstacleGeneratorTest {
    @Test
    fun obstaclesStayInBoundsAndRespawnAfterLeavingScreen() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        val first = generator.snapshot().single()

        assertEquals(0, first.id)
        assertTrue(first.x < 1280.0)
        assertTrue(first.y >= GameEngineConfig.DRAGON_MIN_Y)
        assertTrue(
            first.y + first.size <=
                GameEngineConfig.DRAGON_MAX_Y + GameEngineConfig.DRAGON_SIZE
        )

        repeat(211) {
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
        }

        val respawned = generator.snapshot().first { it.id == 1 }
        assertTrue(generator.snapshot().size > 1)
        assertEquals(1, respawned.id)
        assertTrue(
            respawned.x <=
                1280.0 - GameEngineConfig.INITIAL_OBSTACLE_MARGIN
        )
        assertFalse(respawned.x + respawned.size < 0.0)
    }

    @Test
    fun resetRemovesPreviousObstacleSequence() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        repeat(211) {
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
        }

        generator.reset()

        assertEquals(0, generator.snapshot().single().id)
        assertEquals(
            1280.0 - GameEngineConfig.INITIAL_OBSTACLE_MARGIN,
            generator.snapshot().single().x
        )
    }

    @Test
    fun generatedObstaclesUseAllZoneOneSpritesAndKeepSeparation() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        val spritePaths = mutableSetOf<String>()
        repeat(400) {
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
            spritePaths += generator.snapshot().map { it.spritePath }
        }

        val obstacles = generator.snapshot()
        assertTrue(obstacles.size > 1)
        assertTrue(
            obstacles.zipWithNext().all { (first, second) ->
                second.x - first.x >=
                    GameEngineConfig.MIN_OBSTACLE_SEPARATION -
                    GameEngineConfig.OBSTACLE_SPEED
            }
        )
        assertEquals(6, spritePaths.size)
    }

    @Test
    fun simultaneousObstaclesUseDifferentVerticalEntryPoints() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        repeat(80) {
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
        }

        val obstacles = generator.snapshot()
        assertTrue(obstacles.size >= 2)
        assertEquals(
            obstacles.size,
            obstacles.map { it.y }.toSet().size
        )
    }

    @Test
    fun obstacleLaneDoesNotRepeatMoreThanTwiceConsecutively() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        val lanesById = mutableMapOf<Int, Double>()

        repeat(500) {
            generator.snapshot().forEach { obstacle ->
                lanesById[obstacle.id] = obstacle.y
            }
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
        }

        val lanes = lanesById.toSortedMap().values.toList()
        var consecutiveCount = 1
        lanes.zipWithNext().forEach { (previous, current) ->
            if (current == previous) {
                consecutiveCount += 1
                assertTrue(consecutiveCount <= 2)
            } else {
                consecutiveCount = 1
            }
        }
    }

    @Test
    fun generatedObstaclesIncludeTheBottomLane() {
        val generator = ObstacleGenerator(areaWidth = 1280.0)
        val generatedLanes = mutableSetOf<Double>()

        repeat(120) {
            generatedLanes += generator.snapshot().map { it.y }
            generator.update(GameEngineConfig.MAX_DELTA_SECONDS)
        }

        assertTrue(
            GameEngineConfig.OBSTACLE_BOTTOM_Y in generatedLanes
        )
        assertEquals(
            GameEngineConfig.DRAGON_MAX_Y,
            GameEngineConfig.OBSTACLE_BOTTOM_Y
        )
    }
}
