package com.redcrow.dragonsfirstflight.game.engine

enum class ObstacleHorizontalBehavior(
    val speedMultiplier: Double,
    val separationMultiplier: Double,
) {
    NORMAL(
        speedMultiplier = 1.0,
        separationMultiplier = 1.0,
    ),
    RUSH_MEDIUM(
        speedMultiplier = 1.75,
        separationMultiplier = 1.35,
    ),
    RUSH_HARD(
        speedMultiplier = 2.25,
        separationMultiplier = 1.50,
    ),
    ;

    init {
        require(speedMultiplier.isFinite() && speedMultiplier >= 1.0) {
            "Horizontal speed multiplier must be finite and at least one."
        }
        require(separationMultiplier.isFinite() && separationMultiplier >= 1.0) {
            "Horizontal separation multiplier must be finite and at least one."
        }
    }

    val isRush: Boolean
        get() = this != NORMAL
}
