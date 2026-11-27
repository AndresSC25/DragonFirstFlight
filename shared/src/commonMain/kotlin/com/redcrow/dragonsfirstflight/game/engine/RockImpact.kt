package com.redcrow.dragonsfirstflight.game.engine

/** Captures a stone impact for animation, centered at [x] and [y] in gameplay pixels. */
data class RockImpact(
    val x: Double,
    val y: Double,
    val size: Double,
    /** Enables the extra ground spark when the stone hits the floor. */
    val isGroundImpact: Boolean = false,
    val ageSeconds: Double = 0.0,
) {
    companion object {
        /** Age at which the engine removes the impact from the animation list. */
        const val DURATION_SECONDS: Double = 0.48
    }
}
