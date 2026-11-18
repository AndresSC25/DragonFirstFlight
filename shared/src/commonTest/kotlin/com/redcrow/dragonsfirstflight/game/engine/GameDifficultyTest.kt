package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameDifficultyTest {
    @Test
    fun mediumPreservesTheCurrentBalance() {
        val sampleTimes =
            listOf(
                0.0,
                GameEngineConfig.ZONE_DURATION_SECONDS / 2.0,
                GameEngineConfig.ZONE_DURATION_SECONDS,
                GameEngineConfig.ZONE_DURATION_SECONDS * 2.0,
            )

        sampleTimes.forEach { activeTimeSeconds ->
            assertEquals(
                GameEngineConfig.obstacleSpeedAt(activeTimeSeconds),
                GameDifficulty.MEDIUM.obstacleSpeedAt(activeTimeSeconds),
            )
            assertEquals(
                GameEngineConfig.obstacleSeparationAt(activeTimeSeconds),
                GameDifficulty.MEDIUM.obstacleSeparationAt(activeTimeSeconds),
            )
        }
    }

    @Test
    fun easyIsLessDemandingThanMedium() {
        val activeTimeSeconds = GameEngineConfig.ZONE_DURATION_SECONDS * 2.0

        assertTrue(
            GameDifficulty.EASY.obstacleSpeedAt(activeTimeSeconds) <
                GameDifficulty.MEDIUM.obstacleSpeedAt(activeTimeSeconds),
        )
        assertTrue(
            GameDifficulty.EASY.obstacleSeparationAt(activeTimeSeconds) >
                GameDifficulty.MEDIUM.obstacleSeparationAt(activeTimeSeconds),
        )
    }

    @Test
    fun hardIsMoreDemandingThanMedium() {
        val activeTimeSeconds = GameEngineConfig.ZONE_DURATION_SECONDS * 2.0

        assertTrue(
            GameDifficulty.HARD.obstacleSpeedAt(activeTimeSeconds) >
                GameDifficulty.MEDIUM.obstacleSpeedAt(activeTimeSeconds),
        )
        assertTrue(
            GameDifficulty.HARD.obstacleSeparationAt(activeTimeSeconds) <
                GameDifficulty.MEDIUM.obstacleSeparationAt(activeTimeSeconds),
        )
    }

    @Test
    fun everyDifficultyKeepsPositiveSpeedAndSafeSeparation() {
        val activeTimes =
            listOf(
                0.0,
                GameEngineConfig.ZONE_DURATION_SECONDS,
                GameEngineConfig.ZONE_DURATION_SECONDS * 2.0,
            )

        GameDifficulty.entries.forEach { difficulty ->
            activeTimes.forEach { activeTimeSeconds ->
                assertTrue(difficulty.obstacleSpeedAt(activeTimeSeconds) > 0.0)
                assertTrue(
                    difficulty.obstacleSeparationAt(activeTimeSeconds) >
                        GameEngineConfig.KNIGHT_SWORD_SIZE,
                )
            }
        }
    }

    @Test
    fun difficultiesProvideMenuText() {
        GameDifficulty.entries.forEach { difficulty ->
            assertTrue(difficulty.displayName.isNotBlank())
            assertTrue(difficulty.description.isNotBlank())
        }
    }
}
