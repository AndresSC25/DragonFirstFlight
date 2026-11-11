package com.redcrow.dragonsfirstflight.game.engine

enum class BossAttackStage {
    SINGLE,
    DOUBLE,
    CORRIDOR,
}

enum class ProjectileTrajectory {
    FLOATING,
    ARC,
}

data class BossAttackPattern(
    val stage: BossAttackStage,
    val projectileCenterYs: List<Double>,
    val safeLaneCenterY: Double? = null,
    val isAimed: Boolean = false,
    val trajectory: ProjectileTrajectory = ProjectileTrajectory.FLOATING,
)
