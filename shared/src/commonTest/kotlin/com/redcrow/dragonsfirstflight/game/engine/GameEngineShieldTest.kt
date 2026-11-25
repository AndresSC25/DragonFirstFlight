package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineShieldTest {
    @Test
    fun zoneTwoFieldOffersShieldAndCannotCollectTwice() {
        val field = ShieldPowerUpField(AlwaysSpawnRandom())
        field.advance(
            elapsedSeconds = GameEngineConfig.SHIELD_POWER_UP_FIRST_ATTEMPT_SECONDS,
            distance = 0.0,
            leftBoundary = 0.0,
            isEndless = false,
        )
        field.spawnForNewObstacles(
            obstacles =
                listOf(
                    Obstacle(
                        id = 1,
                        x = 1280.0,
                        y = GameEngineConfig.OBSTACLE_Y,
                        size = GameEngineConfig.OBSTACLE_SIZE,
                        spritePath = "sprites/obstacles/zone2/flying_gargoyle.png",
                    ),
                ),
            rightBoundary = 1280.0,
            zone = GameZone.ZONE_TWO,
            isEndless = false,
        )
        assertTrue(field.powerUps.size == 1)
        val shield = field.powerUps.single()
        field.advance(
            elapsedSeconds = 0.05,
            distance =
                shield.worldX -
                    GameEngineConfig.DRAGON_X,
            leftBoundary = 0.0,
            isEndless = false,
        )
        assertTrue(field.collect(shield.y))
        assertFalse(field.collect(shield.y))
    }

    private class AlwaysSpawnRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
