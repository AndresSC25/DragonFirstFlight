package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObstaclePatternSequencerTest {
    @Test
    fun generatedStepsAlwaysUseConfiguredLanes() {
        GameDifficulty.entries.forEach { difficulty ->
            val sequencer = ObstaclePatternSequencer(Random(7))

            repeat(120) {
                val step =
                    sequencer.nextStep(
                        zone = GameZone.ZONE_THREE,
                        difficulty = difficulty,
                        singleLane = GameEngineConfig.OBSTACLE_Y,
                    )

                assertTrue(
                    step.lane in ObstaclePatternPlanner.allLanes(),
                )
                assertTrue(step.separationMultiplier >= 1.0)
            }
        }
    }

    @Test
    fun completedPatternsDoNotRepeatImmediately() {
        val sequencer = ObstaclePatternSequencer(Random(7))
        val completedPatterns = mutableListOf<ObstaclePattern>()
        var currentPattern: ObstaclePattern? = null

        repeat(300) {
            val step =
                sequencer.nextStep(
                    zone = GameZone.ZONE_THREE,
                    difficulty = GameDifficulty.HARD,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                )

            if (currentPattern != null && step.pattern != currentPattern) {
                completedPatterns += checkNotNull(currentPattern)
            }
            currentPattern = step.pattern
        }

        assertTrue(completedPatterns.size > 10)
        assertTrue(
            completedPatterns.zipWithNext().all { (first, second) ->
                first != second
            },
        )
    }

    @Test
    fun hardZoneThreeEventuallyUsesTheCompleteCatalog() {
        val sequencer = ObstaclePatternSequencer(Random(7))
        val generatedPatterns = mutableSetOf<ObstaclePattern>()

        repeat(1200) {
            generatedPatterns +=
                sequencer.nextStep(
                    zone = GameZone.ZONE_THREE,
                    difficulty = GameDifficulty.HARD,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                ).pattern
        }

        assertEquals(
            ObstaclePattern.entries.toSet(),
            generatedPatterns,
        )
    }

    @Test
    fun aggressivePatternsSuppressRushForEveryStep() {
        val sequencer = ObstaclePatternSequencer(Random(7))
        var aggressiveStepsFound = 0

        repeat(900) {
            val step =
                sequencer.nextStep(
                    zone = GameZone.ZONE_THREE,
                    difficulty = GameDifficulty.HARD,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                )

            if (step.pattern in rushSuppressedPatterns) {
                aggressiveStepsFound++
                assertTrue(step.suppressRush)
            } else {
                assertFalse(step.suppressRush)
            }
        }

        assertTrue(aggressiveStepsFound > 0)
    }

    @Test
    fun intensePatternsProvideRecoveryToTheNextPattern() {
        val sequencer = ObstaclePatternSequencer(Random(11))
        var previousPattern: ObstaclePattern? = null
        var recoveryVerified = false

        repeat(1800) {
            if (!recoveryVerified) {
                val step =
                    sequencer.nextStep(
                        zone = GameZone.ZONE_THREE,
                        difficulty = GameDifficulty.HARD,
                        singleLane = GameEngineConfig.OBSTACLE_Y,
                    )

                if (
                    previousPattern != null &&
                    step.pattern != previousPattern
                ) {
 if (checkNotNull(previousPattern).isIntense) {
                        assertEquals(
                            1.40,
                            step.separationMultiplier,
                            absoluteTolerance = 0.000001,
                        )
                        recoveryVerified = true
                    }
                }
                previousPattern = step.pattern
            }
        }

        assertTrue(
            recoveryVerified,
            "No recovery step was observed after an intense pattern.",
        )
    }

    @Test
    fun resetClearsPendingPatternAndRecovery() {
        val sequencer = ObstaclePatternSequencer(ZeroRandom)
        val first =
            sequencer.nextStep(
                zone = GameZone.ZONE_ONE,
                difficulty = GameDifficulty.EASY,
                singleLane = GameEngineConfig.OBSTACLE_Y,
            )

        repeat(3) {
            sequencer.nextStep(
                zone = GameZone.ZONE_ONE,
                difficulty = GameDifficulty.EASY,
                singleLane = GameEngineConfig.OBSTACLE_Y,
            )
        }

        sequencer.reset()

        val restarted =
            sequencer.nextStep(
                zone = GameZone.ZONE_ONE,
                difficulty = GameDifficulty.EASY,
                singleLane = GameEngineConfig.OBSTACLE_Y,
            )

        assertEquals(first.pattern, restarted.pattern)
        assertEquals(first.lane, restarted.lane)
        assertEquals(1.0, restarted.separationMultiplier)
    }

    private companion object {
        val rushSuppressedPatterns =
            setOf(
                ObstaclePattern.ZIGZAG,
                ObstaclePattern.ALTERNATING,
                ObstaclePattern.DOUBLE_PRESSURE,
            )
    }

    private object ZeroRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
