package com.redcrow.dragonsfirstflight.game.engine

data class RockImpact(
    val x: Double,
    val y: Double,
    val size: Double,
    val isGroundImpact: Boolean = false,
    val ageSeconds: Double = 0.0,
) {
    companion object {
        const val DURATION_SECONDS: Double = 0.48
    }
}
