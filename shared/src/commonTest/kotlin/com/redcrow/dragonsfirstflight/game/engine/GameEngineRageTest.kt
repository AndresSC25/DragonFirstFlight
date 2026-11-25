package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineRageTest {
    @Test
    fun zoneThreeFieldOffersRageAndCannotCollectTwice() {
        val field = RagePowerUpField(AlwaysSpawnRandom())
        field.advance(
            elapsedSeconds = GameEngineConfig.RAGE_POWER_UP_FIRST_ATTEMPT_SECONDS,
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
                        spritePath = "sprites/obstacles/zone3/fairy_sprite.png",
                    ),
                ),
            rightBoundary = 1280.0,
            zone = GameZone.ZONE_THREE,
            isEndless = false,
        )
        assertEquals(1, field.powerUps.size)
        val rage = field.powerUps.single()
        field.advance(
            elapsedSeconds = 0.05,
            distance = rage.worldX - GameEngineConfig.DRAGON_X,
            leftBoundary = 0.0,
            isEndless = false,
        )
        assertTrue(field.collect(rage.y))
        assertFalse(field.collect(rage.y))
    }

    @Test
    fun rageSpeedsWorldInvulnerabilityAndDurationAcrossFrames() {
        val engine =
            GameEngine(
                gameAreaWidth = 1280.0,
                random = Random(7),
                initialMode = GameMode.ENDLESS,
                initialEndlessZone = GameZone.ZONE_THREE,
            )
        val rageField =
            RagePowerUpField(AlwaysSpawnRandom())

        val snapshotBefore = engine.snapshot()
        assertFalse(snapshotBefore.isRageActive)
        assertEquals(snapshotBefore.obstacleSpeed, engine.snapshot().obstacleSpeed)

        engine.restart(mode = GameMode.ENDLESS, endlessZone = GameZone.ZONE_THREE)
        assertFalse(engine.snapshot().isRageActive)
        assertTrue(rageField.powerUps.isEmpty())
    }

    private class AlwaysSpawnRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
