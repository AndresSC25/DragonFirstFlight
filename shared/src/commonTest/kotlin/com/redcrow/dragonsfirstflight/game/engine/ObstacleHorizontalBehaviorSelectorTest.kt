package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObstacleHorizontalBehaviorSelectorTest {
    @Test
    fun easyAlwaysUsesNormalHorizontalBehavior() {
        repeat(500) { seed ->
            assertEquals(
                ObstacleHorizontalBehavior.NORMAL,
                select(
                    difficulty = GameDifficulty.EASY,
                    seed = seed,
                ),
            )
        }
    }

    @Test
    fun mediumCanGenerateRushBehavior() {
        val behaviors =
            (0 until 1000).map { seed ->
                select(
                    difficulty = GameDifficulty.MEDIUM,
                    seed = seed,
                )
            }.toSet()

        assertTrue(ObstacleHorizontalBehavior.NORMAL in behaviors)
        assertTrue(ObstacleHorizontalBehavior.RUSH_MEDIUM in behaviors)
        assertTrue(ObstacleHorizontalBehavior.RUSH_HARD !in behaviors)
    }

    @Test
    fun hardCanGenerateItsFasterRushBehavior() {
        val behaviors =
            (0 until 1000).map { seed ->
                select(
                    difficulty = GameDifficulty.HARD,
                    seed = seed,
                )
            }.toSet()

        assertTrue(ObstacleHorizontalBehavior.NORMAL in behaviors)
        assertTrue(ObstacleHorizontalBehavior.RUSH_HARD in behaviors)
        assertTrue(ObstacleHorizontalBehavior.RUSH_MEDIUM !in behaviors)
    }

    @Test
    fun bottomOnlyBossAndConsecutiveRushAreRejected() {
        GameDifficulty.entries.forEach { difficulty ->
            assertEquals(
                ObstacleHorizontalBehavior.NORMAL,
                select(
                    difficulty = difficulty,
                    isBottomOnly = true,
                ),
            )
            assertEquals(
                ObstacleHorizontalBehavior.NORMAL,
                select(
                    difficulty = difficulty,
                    zone = GameZone.BOSS,
                ),
            )
            assertEquals(
                ObstacleHorizontalBehavior.NORMAL,
                select(
                    difficulty = difficulty,
                    previousWasRush = true,
                ),
            )
        }
    }

    private fun select(
        difficulty: GameDifficulty,
        seed: Int = 7,
        zone: GameZone = GameZone.ZONE_ONE,
        isBottomOnly: Boolean = false,
        previousWasRush: Boolean = false,
    ): ObstacleHorizontalBehavior =
        ObstacleHorizontalBehaviorSelector.select(
            difficulty = difficulty,
            zone = zone,
            isBottomOnly = isBottomOnly,
            previousWasRush = previousWasRush,
            random = Random(seed),
        )
}
