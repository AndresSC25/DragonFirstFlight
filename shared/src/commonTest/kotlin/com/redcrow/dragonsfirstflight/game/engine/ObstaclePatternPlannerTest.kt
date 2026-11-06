package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ObstaclePatternPlannerTest {
    @Test
    fun zoneOneUsesOnlyIntroductoryPatterns() {
        assertEquals(
            listOf(
                ObstaclePattern.SINGLE,
                ObstaclePattern.STAIR_UP,
                ObstaclePattern.STAIR_DOWN,
            ),
            ObstaclePatternPlanner.availablePatterns(GameZone.ZONE_ONE),
        )
    }

    @Test
    fun advancedPatternsAreIntroducedProgressively() {
        assertTrue(
            ObstaclePattern.ZIGZAG in
                ObstaclePatternPlanner.availablePatterns(GameZone.ZONE_TWO),
        )
        assertTrue(
            ObstaclePattern.DIAMOND !in
                ObstaclePatternPlanner.availablePatterns(GameZone.ZONE_TWO),
        )
        assertTrue(
            ObstaclePattern.DIAMOND in
                ObstaclePatternPlanner.availablePatterns(GameZone.ZONE_THREE),
        )
    }

    @Test
    fun everyPatternUsesOnlyConfiguredLanes() {
        ObstaclePattern.entries.forEach { pattern ->
            val generatedLanes =
                ObstaclePatternPlanner.lanesFor(
                    pattern = pattern,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                )

            assertTrue(
                generatedLanes.all {
                    it in ObstaclePatternPlanner.allLanes()
                },
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
}
