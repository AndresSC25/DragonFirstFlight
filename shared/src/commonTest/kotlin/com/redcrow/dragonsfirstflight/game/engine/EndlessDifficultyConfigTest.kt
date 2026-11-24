package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EndlessDifficultyConfigTest {
    @Test
    fun eachDifficultyAcceleratesFromEveryMapAndStopsAtItsMaximum() {
        for (difficulty in GameDifficulty.entries) {
            for (zone in GameZone.entries) {
                val initial = difficulty.endlessStartingSpeed(zone)
                val config = difficulty.endless
                assertEquals(initial, config.speedAt(0.0, initial))
                assertTrue(config.speedAt(30.0, initial) > initial)
                assertTrue(config.speedAt(60.0, initial) >= config.speedAt(30.0, initial))
                assertEquals(config.maximumSpeed, config.speedAt(100_000.0, initial))
                assertEquals(config.maximumSpeed, config.speedAt(Double.MAX_VALUE, initial))
            }
        }
        for (zone in GameZone.entries) {
            val speeds =
                GameDifficulty.entries.map { difficulty ->
                    difficulty.endless.speedAt(60.0, difficulty.endlessStartingSpeed(zone))
                }
            assertTrue(speeds[0] < speeds[1] && speeds[1] < speeds[2])
        }
    }

    @Test
    fun victoryStartsAtTheLastRegularStageSpeedAndKeepsRoomToAccelerate() {
        val expected = listOf(300.0, 345.0, 390.0)
        for ((index, difficulty) in GameDifficulty.entries.withIndex()) {
            val initial = difficulty.endlessStartingSpeed(GameZone.BOSS, afterVictory = true)
            assertEquals(expected[index], initial)
            assertTrue(initial > difficulty.endlessStartingSpeed(GameZone.BOSS))
            assertTrue(difficulty.endless.maximumSpeed > initial)
        }
    }

    @Test
    fun configurationCanChangeTheAccelerationAndLimit() {
        val config = EndlessDifficultyConfig(speedIncreasePerSecond = 2.0, maximumSpeed = 500.0)
        assertEquals(120.0, config.speedAt(10.0, initialSpeed = 100.0))
        assertEquals(500.0, config.speedAt(1000.0, initialSpeed = 100.0))
    }

    @Test
    fun invalidParametersCannotIntroduceNonFiniteOrNegativeSpeeds() {
        for (invalid in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { EndlessDifficultyConfig(invalid, 500.0) }
            assertFailsWith<IllegalArgumentException> { EndlessDifficultyConfig(1.0, invalid) }
        }
        val config = EndlessDifficultyConfig(1.0, 500.0)
        for (invalid in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { config.speedAt(invalid, 100.0) }
        }
        for (invalid in listOf(0.0, -1.0, 501.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { config.speedAt(1.0, invalid) }
        }
    }
}
