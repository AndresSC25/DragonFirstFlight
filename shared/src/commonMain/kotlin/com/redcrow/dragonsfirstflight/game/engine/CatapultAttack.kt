package com.redcrow.dragonsfirstflight.game.engine

/** Timed poses drive both the spritesheet and the single projectile release event. */
internal class CatapultAttack {
    private enum class Phase {
        IDLE,
        LOADING,
        WARNING,
        FIRING,
        RECOVERING,
    }

    private var phase = Phase.IDLE
    private var elapsed = 0.0

    val frameIndex: Int
        get() =
            when (phase) {
                Phase.IDLE -> 0
                Phase.LOADING ->
                    1 +
                        (elapsed / GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS).toInt().coerceAtMost(2)
                Phase.WARNING -> 4
                Phase.FIRING -> 5
                Phase.RECOVERING ->
                    6 +
                        (elapsed / GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS).toInt().coerceAtMost(1)
            }

    val warningFrameIndex: Int?
        get() =
            if (phase == Phase.LOADING || phase == Phase.WARNING) {
                (elapsed / 0.1).toInt() % 8
            } else {
                null
            }

    fun update(
        delta: Double,
        onFire: () -> Unit,
    ) {
        elapsed += delta
        while (elapsed >= phaseDuration()) {
            elapsed -= phaseDuration()
            phase =
                when (phase) {
                    Phase.IDLE -> Phase.LOADING
                    Phase.LOADING -> Phase.WARNING
                    Phase.WARNING -> Phase.FIRING
                    Phase.FIRING -> Phase.RECOVERING
                    Phase.RECOVERING -> Phase.IDLE
                }
            if (phase == Phase.FIRING) onFire()
        }
    }

    fun reset() {
        phase = Phase.IDLE
        elapsed = 0.0
    }

    private fun phaseDuration(): Double =
        when (phase) {
            Phase.IDLE -> GameEngineConfig.CATAPULT_ATTACK_INTERVAL_SECONDS
            Phase.LOADING -> GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS * 3
            Phase.WARNING -> GameEngineConfig.CATAPULT_WARNING_SECONDS
            Phase.FIRING -> GameEngineConfig.CATAPULT_FIRE_SECONDS
            Phase.RECOVERING -> GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS * 2
        }
}
