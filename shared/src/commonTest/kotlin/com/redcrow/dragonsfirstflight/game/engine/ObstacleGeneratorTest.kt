package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class ObstacleGeneratorTest {
    @Test
    fun generatorUsesAllFourLanes() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )
        val generatedLanes = mutableSetOf<Double>()

        repeat(900) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds = frame * 0.05,
            )
            generatedLanes += generator.snapshot().map { it.y }
        }

        assertTrue(
            GameEngineConfig.OBSTACLE_TOP_Y in generatedLanes,
        )
        assertTrue(
            GameEngineConfig.OBSTACLE_Y in generatedLanes,
        )
        assertTrue(
            GameEngineConfig.OBSTACLE_BOTTOM_Y in generatedLanes,
        )
        assertTrue(
            GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y in generatedLanes,
        )
    }

    @Test
    fun mageAndKnightRemainOnTheBottomLane() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )

        repeat(900) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds = frame * 0.05,
            )
            generator
                .snapshot()
                .filter {
                    it.spritePath.endsWith("forest_mage.png") ||
                        it.spritePath.endsWith("knight_sword.png")
                }.forEach { obstacle ->
                    assertTrue(
                        obstacle.y == GameEngineConfig.OBSTACLE_BOTTOM_Y,
                    )
                }
        }
    }

    @Test
    fun laterZonesUseOnlyCrowAndBatObstacles() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )
        val allowedPaths =
            setOf(
                "sprites/obstacles/zone1/flying_bat.png",
                "sprites/obstacles/zone1/flying_crows.png",
            )

        repeat(420) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds =
                    GameEngineConfig.ZONE_DURATION_SECONDS +
                        frame * 0.05,
            )
        }
        assertTrue(
            generator.snapshot().all { it.spritePath in allowedPaths },
        )

        repeat(420) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds =
                    GameEngineConfig.ZONE_DURATION_SECONDS * 2.0 +
                        frame * 0.05,
            )
        }
        assertTrue(
            generator.snapshot().all { it.spritePath in allowedPaths },
        )
    }
}
