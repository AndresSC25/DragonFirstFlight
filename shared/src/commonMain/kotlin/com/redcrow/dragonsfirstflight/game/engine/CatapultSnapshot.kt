package com.redcrow.dragonsfirstflight.game.engine

/** Captures the catapult's screen position in gameplay pixels, attack pose, and warning state for rendering. */
data class CatapultSnapshot(
    val x: Double,
    val y: Double,
    val size: Double,
    val isEntering: Boolean,
    val frameIndex: Int = 0,
    val warningFrameIndex: Int? = null,
    /** Keeps the most recently prepared volley through firing until the next preparation or encounter reset. */
    val attackPattern: BossAttackPattern? = null,
    val isFurious: Boolean = false,
    /** Scales the pulsing red tint from 0 to 1 during fury. */
    val furyPulse: Double = 0.0,
)
