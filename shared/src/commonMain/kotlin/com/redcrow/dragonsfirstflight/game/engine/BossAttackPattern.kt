package com.redcrow.dragonsfirstflight.game.engine

/** Identifies the volley layout available during each part of the boss fight. */
enum class BossAttackStage {
    SINGLE,
    DOUBLE,

    /** Fills the play area with stones above and below a safe lane. */
    CORRIDOR,
}

/** Selects whether a boss stone eases into formation and bobs or follows a gravity-driven arc. */
enum class ProjectileTrajectory {
    FLOATING,
    ARC,
}

/** Holds the volley selected during preparation for use when the catapult fires. */
data class BossAttackPattern(
    val stage: BossAttackStage,
    /** Target stone center heights in gameplay pixels. A double volley fires in list order. */
    val projectileCenterYs: List<Double>,
    /** Center of the gap in gameplay pixels, or null for volleys without a corridor. */
    val safeLaneCenterY: Double? = null,
    /** Marks a volley with a stone aimed at the dragon's height when the catapult starts loading. */
    val isAimed: Boolean = false,
    val trajectory: ProjectileTrajectory = ProjectileTrajectory.FLOATING,
)
