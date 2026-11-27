package com.redcrow.dragonsfirstflight.game.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreTrackerTest {
    @Test
    fun fractionalFramesAccumulateWithoutLosingWholeSeconds() {
        val tracker = ScoreTracker(pointsPerSecond = 1)

        repeat(19) { tracker.update(0.05) }
        assertEquals(0, tracker.score.points)
        tracker.update(0.05)
        assertEquals(1, tracker.score.points)

        repeat(80) { tracker.update(0.05) }
        assertEquals(Score(points = 5, activeTimeSeconds = 5.0), tracker.score)
    }

    @Test
    fun configuredRateProducesTheSameScoreAcrossFrameSizes() {
        val fragmented = ScoreTracker(pointsPerSecond = 10)
        val combined = ScoreTracker(pointsPerSecond = 10)

        repeat(125) { fragmented.update(0.01) }
        combined.update(1.25)

        assertEquals(Score(points = 12, activeTimeSeconds = 1.25), combined.score)
        assertEquals(combined.score, fragmented.score)
    }

    @Test
    fun pointsMultiplierSpeedsUpScoringWithoutAdvancingActiveTime() {
        val tracker = ScoreTracker(pointsPerSecond = 1)

        tracker.update(elapsedSeconds = 2.0)
        tracker.update(elapsedSeconds = 2.0, pointsMultiplier = 2.5)

        assertEquals(7, tracker.score.points)
        assertEquals(4.0, tracker.score.activeTimeSeconds)
    }

    @Test
    fun scoringMultiplierCanChangeWithoutLosingFractionalProgress() {
        val tracker = ScoreTracker(pointsPerSecond = 1)

        tracker.update(elapsedSeconds = 0.4)
        tracker.update(elapsedSeconds = 0.2, pointsMultiplier = 2.5)
        tracker.update(elapsedSeconds = 0.1)

        assertEquals(1, tracker.score.points)
        assertEquals(0.7, tracker.score.activeTimeSeconds, 1e-9)
    }

    @Test
    fun invalidElapsedTimeDoesNotChangeTheScore() {
        val tracker = ScoreTracker(pointsPerSecond = 1)
        tracker.update(1.5)
        val previousScore = tracker.score

        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            tracker.update(it)
        }

        assertEquals(previousScore, tracker.score)
    }
}
