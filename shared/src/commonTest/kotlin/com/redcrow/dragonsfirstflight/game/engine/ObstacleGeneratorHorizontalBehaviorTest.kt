package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObstacleGeneratorHorizontalBehaviorTest {
    @Test
    fun easyNeverGeneratesRushObstacles() {
        val generated =
            collectGeneratedObstacles(
                difficulty = GameDifficulty.EASY,
                seed = 7,
            )

        assertTrue(generated.isNotEmpty())
        assertTrue(
            generated.all { obstacle ->
                obstacle.horizontalBehavior ==
                    ObstacleHorizontalBehavior.NORMAL
            },
        )
    }

    @Test
    fun mediumGeneratesOnlyItsRushVariant() {
        val generated =
            collectGeneratedObstacles(
                difficulty = GameDifficulty.MEDIUM,
                seed = 7,
            )

        assertTrue(
            generated.any { obstacle ->
                obstacle.horizontalBehavior ==
                    ObstacleHorizontalBehavior.RUSH_MEDIUM
            },
        )
        assertTrue(
            generated.none { obstacle ->
                obstacle.horizontalBehavior ==
                    ObstacleHorizontalBehavior.RUSH_HARD
            },
        )
    }

    @Test
    fun hardGeneratesOnlyItsFasterRushVariant() {
        val generated =
            collectGeneratedObstacles(
                difficulty = GameDifficulty.HARD,
                seed = 7,
            )

        assertTrue(
            generated.any { obstacle ->
                obstacle.horizontalBehavior ==
                    ObstacleHorizontalBehavior.RUSH_HARD
            },
        )
        assertTrue(
            generated.none { obstacle ->
                obstacle.horizontalBehavior ==
                    ObstacleHorizontalBehavior.RUSH_MEDIUM
            },
        )
    }

    @Test
    fun rushObstaclesAreStaticAndNeverConsecutive() {
        val generated =
            collectGeneratedObstacles(
                difficulty = GameDifficulty.HARD,
                seed = 7,
            )
        val ordered = generated.sortedBy { obstacle -> obstacle.id }

        assertEquals(
            ObstacleHorizontalBehavior.NORMAL,
            ordered.first().horizontalBehavior,
        )

        ordered
            .filter { obstacle -> obstacle.horizontalBehavior.isRush }
            .forEach { obstacle ->
                assertEquals(ObstacleMovement.STATIC, obstacle.movement)
            }

        ordered.zipWithNext().forEach { (first, second) ->
            assertFalse(
                first.horizontalBehavior.isRush &&
                    second.horizontalBehavior.isRush,
                "Rush obstacles must not be generated consecutively.",
            )
        }
    }

    @Test
    fun bottomOnlyObstaclesNeverUseRushBehavior() {
        val generated =
            collectGeneratedObstacles(
                difficulty = GameDifficulty.HARD,
                seed = 7,
            )

        generated
            .filter { obstacle ->
                obstacle.spritePath.endsWith("forest_mage.png") ||
                    obstacle.spritePath.endsWith("knight_sword.png")
            }.forEach { obstacle ->
                assertEquals(
                    ObstacleHorizontalBehavior.NORMAL,
                    obstacle.horizontalBehavior,
                )
            }
    }

    @Test
    fun rushObstacleUsesItsHorizontalSpeedMultiplier() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
                difficulty = GameDifficulty.HARD,
            )
        val delta = 0.05
        var verified = false

        repeat(MAX_FRAMES) { frame ->
            if (!verified) {
                val activeTimeSeconds = frame * delta
                val before = generator.snapshot()
                val rushBefore =
                    before.firstOrNull { obstacle ->
                        obstacle.horizontalBehavior.isRush &&
                            obstacle.rushWarningRemainingSeconds == 0.0
                    }

                generator.update(
                    delta = delta,
                    activeTimeSeconds = activeTimeSeconds,
                )

                if (rushBefore != null) {
                    val rushAfter =
                        assertNotNull(
                            generator
                                .snapshot()
                                .firstOrNull { obstacle ->
                                    obstacle.id == rushBefore.id
                                },
                        )
                    val expectedDistance =
                        GameDifficulty.HARD
                            .obstacleSpeedAt(activeTimeSeconds) *
                            rushBefore.horizontalBehavior.speedMultiplier *
                            delta

                    assertEquals(
                        expectedDistance,
                        rushBefore.x - rushAfter.x,
                        absoluteTolerance = 0.000001,
                    )
                    verified = true
                }
            }
        }

        assertTrue(
            verified,
            "No active rush obstacle was available for movement verification.",
        )
    }

    private fun collectGeneratedObstacles(
        difficulty: GameDifficulty,
        seed: Int,
    ): List<Obstacle> {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(seed),
                difficulty = difficulty,
            )
        val generatedById = linkedMapOf<Int, Obstacle>()

        repeat(MAX_FRAMES) { frame ->
            generator.snapshot().forEach { obstacle ->
                generatedById.putIfAbsent(obstacle.id, obstacle)
            }
            generator.update(
                delta = FRAME_DELTA,
                activeTimeSeconds = frame * FRAME_DELTA,
            )
        }

        generator.snapshot().forEach { obstacle ->
            generatedById.putIfAbsent(obstacle.id, obstacle)
        }

        return generatedById.values.toList()
    }

    private companion object {
        const val MAX_FRAMES = 1800
        const val FRAME_DELTA = 0.05
    }
}
