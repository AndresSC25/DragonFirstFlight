package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObstacleHorizontalBehaviorTest {
    @Test
    fun normalBehaviorPreservesOriginalMovementValues() {
        val behavior = ObstacleHorizontalBehavior.NORMAL

        assertEquals(1.0, behavior.speedMultiplier)
        assertEquals(1.0, behavior.separationMultiplier)
        assertFalse(behavior.isRush)
    }

    @Test
    fun rushBehaviorsIncreaseSpeedAndRequiredSeparation() {
        val medium = ObstacleHorizontalBehavior.RUSH_MEDIUM
        val hard = ObstacleHorizontalBehavior.RUSH_HARD

        assertTrue(medium.isRush)
        assertTrue(hard.isRush)
        assertTrue(medium.speedMultiplier > 1.0)
        assertTrue(medium.separationMultiplier > 1.0)
        assertTrue(hard.speedMultiplier > medium.speedMultiplier)
        assertTrue(hard.separationMultiplier > medium.separationMultiplier)
    }

    @Test
    fun obstacleUsesNormalHorizontalBehaviorByDefault() {
        val obstacle =
            Obstacle(
                id = 1,
                x = 800.0,
                y = GameEngineConfig.OBSTACLE_Y,
                size = GameEngineConfig.OBSTACLE_SIZE,
                spritePath = "test/obstacle.png",
            )

        assertEquals(
            ObstacleHorizontalBehavior.NORMAL,
            obstacle.horizontalBehavior,
        )
    }

    @Test
    fun obstacleCanStoreRushHorizontalBehavior() {
        val obstacle =
            Obstacle(
                id = 1,
                x = 800.0,
                y = GameEngineConfig.OBSTACLE_Y,
                size = GameEngineConfig.OBSTACLE_SIZE,
                spritePath = "test/obstacle.png",
                horizontalBehavior = ObstacleHorizontalBehavior.RUSH_HARD,
            )

        assertEquals(
            ObstacleHorizontalBehavior.RUSH_HARD,
            obstacle.horizontalBehavior,
        )
        assertEquals(2.25, obstacle.horizontalBehavior.speedMultiplier)
    }
}
