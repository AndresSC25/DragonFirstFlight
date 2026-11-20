package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObstaclePatternPlannerTest {
    @Test
    fun easyUsesOnlyPredictablePatterns() {
        GameZone.entries
            .filter { zone -> zone != GameZone.BOSS }
            .forEach { zone ->
                val patterns =
                    ObstaclePatternPlanner.availablePatterns(
                        zone = zone,
                        difficulty = GameDifficulty.EASY,
                    )

                assertTrue(patterns.isNotEmpty())
                assertTrue(
                    patterns.all { pattern ->
                        pattern in predictablePatterns
                    },
                )
                assertTrue(patterns.none { pattern -> pattern.isIntense })
            }
    }

    @Test
    fun mediumIntroducesAdvancedPatternsProgressively() {
        val zoneOne =
            ObstaclePatternPlanner.availablePatterns(
                zone = GameZone.ZONE_ONE,
                difficulty = GameDifficulty.MEDIUM,
            )
        val zoneTwo =
            ObstaclePatternPlanner.availablePatterns(
                zone = GameZone.ZONE_TWO,
                difficulty = GameDifficulty.MEDIUM,
            )
        val zoneThree =
            ObstaclePatternPlanner.availablePatterns(
                zone = GameZone.ZONE_THREE,
                difficulty = GameDifficulty.MEDIUM,
            )

        assertTrue(ObstaclePattern.MIDDLE_WEAVE in zoneOne)
        assertTrue(ObstaclePattern.FAKE_OPENING in zoneOne)
        assertFalse(ObstaclePattern.DOUBLE_PRESSURE in zoneOne)
        assertTrue(ObstaclePattern.ZIGZAG in zoneTwo)
        assertTrue(ObstaclePattern.DIAMOND in zoneTwo)
        assertTrue(ObstaclePattern.ALTERNATING in zoneTwo)
        assertFalse(ObstaclePattern.DOUBLE_PRESSURE in zoneTwo)
        assertTrue(ObstaclePattern.DOUBLE_PRESSURE in zoneThree)
    }

    @Test
    fun hardZoneThreeProvidesTheCompleteCatalog() {
        val patterns =
            ObstaclePatternPlanner.availablePatterns(
                zone = GameZone.ZONE_THREE,
                difficulty = GameDifficulty.HARD,
            )

        assertEquals(
            ObstaclePattern.entries.toSet(),
            patterns.toSet(),
        )
        assertTrue(
            patterns.count { pattern -> pattern.isIntense } >
                patterns.count { pattern -> !pattern.isIntense },
        )
    }

    @Test
    fun bossUsesItsIndependentPatternSystem() {
        GameDifficulty.entries.forEach { difficulty ->
            assertTrue(
                ObstaclePatternPlanner
                    .availablePatterns(
                        zone = GameZone.BOSS,
                        difficulty = difficulty,
                    ).isEmpty(),
            )
        }
    }

    @Test
    fun everyPatternAndVariantUsesOnlyConfiguredLanes() {
        ObstaclePattern.entries.forEach { pattern ->
            listOf(false, true).forEach { reverseVariant ->
                val lanes =
                    ObstaclePatternPlanner.lanesFor(
                        pattern = pattern,
                        singleLane = GameEngineConfig.OBSTACLE_Y,
                        reverseVariant = reverseVariant,
                    )

                assertTrue(lanes.isNotEmpty())
                assertTrue(
                    lanes.all { lane ->
                        lane in ObstaclePatternPlanner.allLanes()
                    },
                )
            }
        }
    }

    @Test
    fun reversiblePatternsProduceDistinctVariants() {
        reversiblePatterns.forEach { pattern ->
            val normal =
                ObstaclePatternPlanner.lanesFor(
                    pattern = pattern,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                    reverseVariant = false,
                )
            val reversed =
                ObstaclePatternPlanner.lanesFor(
                    pattern = pattern,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                    reverseVariant = true,
                )

            assertTrue(
                normal != reversed,
                "Expected $pattern to provide two distinct variants.",
            )
        }
    }

    @Test
    fun invalidSingleLaneIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            ObstaclePatternPlanner.lanesFor(
                pattern = ObstaclePattern.SINGLE,
                singleLane = -1.0,
            )
        }
    }

    private companion object {
        val predictablePatterns =
            setOf(
                ObstaclePattern.SINGLE,
                ObstaclePattern.STAIR_UP,
                ObstaclePattern.STAIR_DOWN,
                ObstaclePattern.MIDDLE_WEAVE,
            )

        val reversiblePatterns =
            setOf(
                ObstaclePattern.ALTERNATING,
                ObstaclePattern.MIDDLE_WEAVE,
                ObstaclePattern.FAKE_OPENING,
                ObstaclePattern.DOUBLE_PRESSURE,
            )
    }
}
