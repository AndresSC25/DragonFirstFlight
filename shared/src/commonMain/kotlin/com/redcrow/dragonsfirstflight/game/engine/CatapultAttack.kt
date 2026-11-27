package com.redcrow.dragonsfirstflight.game.engine

/** Preparation locks the warned pattern; the firing pose releases it exactly once. */
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
    private var furious = false

    /** Selects the catapult sprite frame for the current attack phase and elapsed time. */
    val frameIndex: Int
        get() =
            when (phase) {
                Phase.IDLE -> 0
                Phase.LOADING ->
                    1 +
                        (elapsed / loadFrameSeconds()).toInt().coerceAtMost(2)
                Phase.WARNING -> 4
                Phase.FIRING -> 5
                Phase.RECOVERING ->
                    6 +
                        (elapsed / GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS).toInt().coerceAtMost(1)
            }

    /** Loops the warning sprite during loading and warning phases and returns null otherwise. */
    val warningFrameIndex: Int?
        get() =
            if (phase == Phase.LOADING || phase == Phase.WARNING) {
                (elapsed / 0.1).toInt() % 8
            } else {
                null
            }

    /** Advances attack time in seconds, calling [onPrepare] on entry to loading and [onFire] on entry to firing. */
    fun update(
        delta: Double,
        isFurious: Boolean = false,
        onPrepare: () -> Unit = {},
        onFire: () -> Unit,
    ) {
        if (furious != isFurious) {
            // Preserve the current pose/progress when entering fury; never skip a warning.
            val progress = elapsed / phaseDuration()
            furious = isFurious
            elapsed = progress * phaseDuration()
        }
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
            if (phase == Phase.LOADING) onPrepare()
            if (phase == Phase.FIRING) onFire()
        }
    }

    /** Returns to the idle pose and restores normal attack timing. */
    fun reset() {
        phase = Phase.IDLE
        elapsed = 0.0
        furious = false
    }

    private fun phaseDuration(): Double =
        when (phase) {
            Phase.IDLE ->
                if (furious) {
                    GameEngineConfig.CATAPULT_FURY_ATTACK_INTERVAL_SECONDS
                } else {
                    GameEngineConfig.CATAPULT_ATTACK_INTERVAL_SECONDS
                }
            Phase.LOADING -> loadFrameSeconds() * 3
            Phase.WARNING -> GameEngineConfig.CATAPULT_WARNING_SECONDS
            Phase.FIRING -> GameEngineConfig.CATAPULT_FIRE_SECONDS
            Phase.RECOVERING -> GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS * 2
        }

    private fun loadFrameSeconds(): Double =
        if (furious) GameEngineConfig.CATAPULT_FURY_LOAD_FRAME_SECONDS else GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS
}
