package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObstacleMovementSelectorTest {
    @Test
    fun zoneOneUsesOnlyStaticMovement() {
        repeat(50) { seed ->
            assertEquals(
                ObstacleMovement.STATIC,
                ObstacleMovementSelector.select(
                    zone = GameZone.ZONE_ONE,
                    isBottomOnly = false,
                    random = Random(seed),
                ),
            )
        }
    }

    @Test
    fun bottomOnlyObstaclesAlwaysRemainStatic() {
        GameZone.entries.forEach { zone ->
            repeat(50) { seed ->
                assertEquals(
                    ObstacleMovement.STATIC,
                    ObstacleMovementSelector.select(
                        zone = zone,
                        isBottomOnly = true,
                        random = Random(seed),
                    ),
                )
            }
        }
    }

    @Test
    fun zoneTwoIntroducesBobMovement() {
        val movements =
            (0 until 100).map { seed ->
                ObstacleMovementSelector.select(
                    zone = GameZone.ZONE_TWO,
                    isBottomOnly = false,
                    random = Random(seed),
                )
            }.toSet()

        assertTrue(ObstacleMovement.STATIC in movements)
        assertTrue(ObstacleMovement.BOB in movements)
        assertTrue(ObstacleMovement.WAVE !in movements)
    }

    @Test
    fun zoneThreeIntroducesAllMovementTypes() {
        val movements =
            (0 until 200).map { seed ->
                ObstacleMovementSelector.select(
                    zone = GameZone.ZONE_THREE,
                    isBottomOnly = false,
                    random = Random(seed),
                )
            }.toSet()

        assertEquals(ObstacleMovement.entries.toSet(), movements)
    }
}
