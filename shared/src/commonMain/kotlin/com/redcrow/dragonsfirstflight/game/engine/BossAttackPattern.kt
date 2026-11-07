package com.redcrow.dragonsfirstflight.game.engine

enum class BossAttackStage {
    SINGLE,
    DOUBLE,
    CORRIDOR,
}

data class BossAttackPattern(
    val stage: BossAttackStage,
    val projectileCenterYs: List<Double>,
    val safeLaneCenterY: Double? = null,
)
