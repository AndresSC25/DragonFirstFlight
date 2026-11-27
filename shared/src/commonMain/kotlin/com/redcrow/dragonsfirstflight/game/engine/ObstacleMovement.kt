package com.redcrow.dragonsfirstflight.game.engine

/** Vertical motion style of an obstacle. */
enum class ObstacleMovement {
    STATIC,

    /** Small slow sine oscillation. */
    BOB,

    /** Larger and faster sine oscillation than [BOB]. */
    WAVE,
}
