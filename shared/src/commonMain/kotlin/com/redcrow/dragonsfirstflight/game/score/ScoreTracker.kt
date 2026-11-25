package com.redcrow.dragonsfirstflight.game.score

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class ScoreTracker(
    private val pointsPerSecond: Int,
) {
    private var activeTime: Duration = Duration.ZERO
    private var scoringTime: Duration = Duration.ZERO

    var score: Score = Score()
        private set

    init {
        require(pointsPerSecond > 0) {
            "Points per second must be greater than zero."
        }
    }

    fun update(
        elapsedSeconds: Double,
        pointsMultiplier: Double = 1.0,
    ) {
        if (
            !elapsedSeconds.isFinite() ||
            elapsedSeconds <= 0.0 ||
            !pointsMultiplier.isFinite() ||
            pointsMultiplier <= 0.0
        ) {
            return
        }

        val nextActiveTime = activeTime + elapsedSeconds.seconds
        val scaledScoringSeconds = elapsedSeconds * pointsMultiplier
        if (!scaledScoringSeconds.isFinite()) {
            return
        }
        val nextScoringTime = scoringTime + scaledScoringSeconds.seconds
        if (!nextActiveTime.isFinite() || !nextScoringTime.isFinite()) return

        activeTime = nextActiveTime
        scoringTime = nextScoringTime
        val activeTimeSeconds = activeTime.toDouble(DurationUnit.SECONDS)
        val scoringTimeSeconds = scoringTime.toDouble(DurationUnit.SECONDS)
        score =
            Score(
                points = (scoringTimeSeconds * pointsPerSecond).toInt(),
                activeTimeSeconds = activeTimeSeconds,
            )
    }

    fun reset() {
        activeTime = Duration.ZERO
        scoringTime = Duration.ZERO
        score = Score()
    }
}
