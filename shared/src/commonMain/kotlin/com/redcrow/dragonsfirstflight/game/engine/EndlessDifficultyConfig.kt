package com.redcrow.dragonsfirstflight.game.engine

/** Defines the linear speed increase and upper limit for Endless obstacle scrolling. */
data class EndlessDifficultyConfig(
    /** Increase in scrolling speed, in pixels per second, for each second of Endless play. */
    val speedIncreasePerSecond: Double,
    /** Upper limit in pixels per second before the rage multiplier applies. */
    val maximumSpeed: Double,
) {
    init {
        require(speedIncreasePerSecond.isFinite() && speedIncreasePerSecond > 0.0) {
            "Endless speed increase must be finite and greater than zero."
        }
        require(maximumSpeed.isFinite() && maximumSpeed > 0.0) {
            "Endless maximum speed must be finite and greater than zero."
        }
    }

    /** Returns the scrolling speed in pixels per second, capped at [maximumSpeed] after linear acceleration. */
    fun speedAt(
        elapsedSeconds: Double,
        initialSpeed: Double,
    ): Double {
        require(elapsedSeconds.isFinite() && elapsedSeconds >= 0.0) {
            "Endless elapsed time must be finite and must not be negative."
        }
        require(initialSpeed.isFinite() && initialSpeed > 0.0 && initialSpeed <= maximumSpeed) {
            "Endless initial speed must be positive and must not exceed its maximum."
        }
        return (initialSpeed + elapsedSeconds * speedIncreasePerSecond).coerceAtMost(maximumSpeed)
    }
}
