package com.redcrow.dragonsfirstflight.game.engine

/** How fast an obstacle approaches and how much extra gap it needs from the previous one. */
enum class ObstacleHorizontalBehavior(
    /** Factor applied to the base obstacle speed. */
    val speedMultiplier: Double,
    /** Factor applied to the spawn gap behind the previous obstacle. */
    val separationMultiplier: Double,
) {
    NORMAL(
        speedMultiplier = 1.0,
        separationMultiplier = 1.0,
    ),

    /** Faster obstacle announced by a short warning, used on medium difficulty. */
    RUSH_MEDIUM(
        speedMultiplier = 1.75,
        separationMultiplier = 1.35,
    ),

    /** Fastest obstacle announced by a short warning, used on hard difficulty. */
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
