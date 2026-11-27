package com.redcrow.dragonsfirstflight.game.engine

/** Stores a shot's top-left position in gameplay pixels and its launch velocities in pixels per second. */
data class Projectile(
    val id: Int,
    val x: Double,
    val y: Double,
    val size: Double,
    val velocityX: Double,
    val velocityY: Double,
    val ageSeconds: Double = 0.0,
    /** Launch top edge in gameplay pixels, used to compute vertical movement from the shot's age. */
    val baseY: Double = y,
    /** Target top edge in gameplay pixels before bobbing and vertical drift on a floating shot. */
    val targetY: Double = y,
    /** Horizontal distance in gameplay pixels over which a floating shot eases toward [targetY]. */
    val formationDistance: Double = 0.0,
    /** Maximum bobbing offset from the floating path in gameplay pixels. */
    val floatAmplitude: Double = 0.0,
    /** Positive downward acceleration in pixels per second squared selects a ballistic arc. */
    val gravity: Double = 0.0,
)
