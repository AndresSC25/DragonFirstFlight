package com.redcrow.dragonsfirstflight.game.engine

/** Pairs the volley pattern with one stone's target center Y in gameplay pixels. */
internal data class BossProjectileLaunch(
    val pattern: BossAttackPattern,
    val centerY: Double,
)

/** Holds the second stone's locked target and drives its quick firing/recovery poses. */
internal class BossVolley {
    private var pending: BossProjectileLaunch? = null
    private var delayRemaining = 0.0
    private var secondaryAnimationElapsed: Double? = null

    /** Returns the second stone's firing or recovery frame, or null when the main pose applies. */
    val secondaryFrameIndex: Int?
        get() {
            val elapsed = secondaryAnimationElapsed ?: return null
            return if (elapsed < GameEngineConfig.CATAPULT_FIRE_SECONDS) {
                5
            } else {
                val recoveryElapsed = elapsed - GameEngineConfig.CATAPULT_FIRE_SECONDS
                6 + (recoveryElapsed / GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS).toInt().coerceAtMost(1)
            }
        }

    /** Returns the launches due now and defers the second stone of a double volley, replacing any pending launch. */
    fun start(pattern: BossAttackPattern): List<BossProjectileLaunch> {
        reset()
        val launches = pattern.projectileCenterYs.map { BossProjectileLaunch(pattern, it) }
        if (pattern.stage == BossAttackStage.DOUBLE) {
            pending = launches[1]
            delayRemaining = GameEngineConfig.BOSS_DOUBLE_SHOT_DELAY_SECONDS
            return launches.take(1)
        }
        return launches
    }

    /** Advances the delay and pose by [delta] seconds and emits the delayed launch as soon as it is due. */
    fun update(delta: Double): List<BossProjectileLaunch> {
        secondaryAnimationElapsed?.let { elapsed ->
            val duration = GameEngineConfig.CATAPULT_FIRE_SECONDS + GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS * 2
            secondaryAnimationElapsed = (elapsed + delta).takeIf { it < duration }
        }
        val launch = pending ?: return emptyList()
        delayRemaining -= delta
        if (delayRemaining > 0.000000001) return emptyList()

        pending = null
        secondaryAnimationElapsed = 0.0
        return listOf(launch)
    }

    /** Cancels the delayed launch and clears its firing and recovery pose. */
    fun reset() {
        pending = null
        delayRemaining = 0.0
        secondaryAnimationElapsed = null
    }
}
