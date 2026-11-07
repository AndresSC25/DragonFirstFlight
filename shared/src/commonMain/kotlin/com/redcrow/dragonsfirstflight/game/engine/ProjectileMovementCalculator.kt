package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

internal object ProjectileMovementCalculator {
    fun yAt(
        projectile: Projectile,
        ageSeconds: Double,
    ): Double {
        val progress =
            if (projectile.formationDistance > 0.0) {
                (abs(projectile.velocityX) * ageSeconds / projectile.formationDistance).coerceIn(0.0, 1.0)
            } else {
                1.0
            }
        val easedProgress = progress * progress * (3.0 - 2.0 * progress)
        val formationY = projectile.baseY + (projectile.targetY - projectile.baseY) * easedProgress
        // Every rock in a volley has the same age and phase, preserving the corridor while bobbing.
        val floating =
            sin(ageSeconds * GameEngineConfig.BOSS_PROJECTILE_FLOAT_CYCLES_PER_SECOND * 2.0 * PI) *
                projectile.floatAmplitude
        return formationY + projectile.velocityY * ageSeconds + floating
    }
}
