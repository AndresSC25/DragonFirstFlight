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
    fun zoneTwoUsesItsConfiguredObstacles() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )
        val zoneTwoPaths =
            setOf(
                "sprites/obstacles/zone2/flying _skull_wisp.png",
                "sprites/obstacles/zone2/flying_cursed_book.png",
                "sprites/obstacles/zone2/flying_gargoyle.png",
                "sprites/obstacles/zone2/ghost_knight.png",
                "sprites/obstacles/zone2/winged_imp.png",
            )

        repeat(300) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds =
                    GameEngineConfig.ZONE_DURATION_SECONDS +
                        frame * 0.05,
            )
        }
        assertTrue(
            generator.snapshot().all { it.spritePath in zoneTwoPaths },
        )
        assertTrue(generator.snapshot().isNotEmpty())
    }

    @Test
    fun selectedZoneTwoObstaclesUseTheirLargerSizes() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )

        repeat(300) { frame ->
            generator.update(
                delta = 0.05,
                activeTimeSeconds =
                    GameEngineConfig.ZONE_DURATION_SECONDS +
                        frame * 0.05,
            )
        }

        generator
            .snapshot()
            .filter {
                it.spritePath.endsWith("flying_gargoyle.png") ||
                    it.spritePath.endsWith("ghost_knight.png")
            }.forEach { obstacle ->
                assertTrue(
                    obstacle.size ==
                        if (obstacle.spritePath.endsWith("flying_gargoyle.png")) {
                            GameEngineConfig.ZONE_TWO_FLYING_GARGOYLE_SIZE
                        } else {
                            GameEngineConfig.ZONE_TWO_GHOST_KNIGHT_SIZE
                        },
                )
            }
    }

    @Test
    fun zoneThreeUsesItsConfiguredObstacles() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
            )
        val zoneThreePaths =
            setOf(
                "sprites/obstacles/zone3/fairy_sprite.png",
                "sprites/obstacles/zone3/flying_forest_monster.png",
                "sprites/obstacles/zone3/giant_hornet.png",
                "sprites/obstacles/zone3/wind_spirit.png",
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
            generator.snapshot().all { it.spritePath in zoneThreePaths },
        )
    }
}
