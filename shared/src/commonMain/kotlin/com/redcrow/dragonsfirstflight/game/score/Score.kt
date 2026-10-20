package com.redcrow.dragonsfirstflight.game.score

/** Represents the score and accumulated active time of a game session. */
data class Score(
    val points: Int = 0,
    val activeTimeSeconds: Double = 0.0
) {
    init {
        require(points >= 0) {
            "Points must not be negative."
        }
        require(activeTimeSeconds.isFinite() && activeTimeSeconds >= 0.0) {
            "Active time must be finite and must not be negative."
        }
    }
}
