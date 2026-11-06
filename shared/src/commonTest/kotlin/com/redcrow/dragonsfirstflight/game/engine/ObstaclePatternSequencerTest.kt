package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObstaclePatternSequencerTest {
    @Test
    fun zoneOneProducesOnlyConfiguredLanes() {
        val sequencer = ObstaclePatternSequencer(Random(7))

        repeat(20) {
            val lane =
                sequencer.nextLane(
                    zone = GameZone.ZONE_ONE,
                    singleLane = GameEngineConfig.OBSTACLE_Y,
                )
            assertTrue(lane in ObstaclePatternPlanner.allLanes())
        }
    }

    @Test
    fun resetClearsThePendingPattern() {
        val sequencer = ObstaclePatternSequencer(ZeroRandom)

        val firstLane =
            sequencer.nextLane(
                zone = GameZone.ZONE_ONE,
                singleLane = GameEngineConfig.OBSTACLE_Y,
            )
        sequencer.nextLane(
            zone = GameZone.ZONE_ONE,
            singleLane = GameEngineConfig.OBSTACLE_Y,
        )

        sequencer.reset()

        val laneAfterReset =
            sequencer.nextLane(
                zone = GameZone.ZONE_ONE,
                singleLane = GameEngineConfig.OBSTACLE_Y,
            )

        assertEquals(firstLane, laneAfterReset)
    }

    private object ZeroRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }
}
