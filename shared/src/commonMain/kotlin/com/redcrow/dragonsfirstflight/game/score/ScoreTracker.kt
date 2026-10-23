package com.redcrow.dragonsfirstflight.game.score

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class ScoreTracker(
    private val pointsPerSecond: Int,
) {
    private var activeTime: Duration = Duration.ZERO

    var score: Score = Score()
        private set

    init {
        require(pointsPerSecond > 0) {
            "Points per second must be greater than zero."
        }
    }

    fun update(elapsedSeconds: Double) {
        if (!elapsedSeconds.isFinite() || elapsedSeconds <= 0.0) {
            return
        }

        val nextActiveTime = activeTime + elapsedSeconds.seconds
        if (!nextActiveTime.isFinite()) {
            return
        }

        activeTime = nextActiveTime
        val activeTimeSeconds = activeTime.toDouble(DurationUnit.SECONDS)
        score =
            Score(
                points = (activeTimeSeconds * pointsPerSecond).toInt(),
                activeTimeSeconds = activeTimeSeconds,
            )
    }

    fun reset() {
        activeTime = Duration.ZERO
        score = Score()
    }
}
