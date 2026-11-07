package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectileMovementCalculatorTest {
    private fun rock(targetY: Double) =
        Projectile(
            id = 0,
            x = 900.0,
            y = 395.2,
            size = 64.0,
            velocityX = -400.0,
            velocityY = 0.0,
            targetY = targetY,
            formationDistance = 160.0,
            floatAmplitude = 16.0,
        )

    @Test
    fun startsAtLauncherThenReachesLaneAndFloatsWithinReservedEnvelope() {
        val projectile = rock(228.0)
        assertEquals(projectile.y, ProjectileMovementCalculator.yAt(projectile, 0.0))
        val middle = ProjectileMovementCalculator.yAt(projectile, 0.2)
        assertTrue(middle < projectile.y && middle > projectile.targetY)
        val positions = (0..100).map { ProjectileMovementCalculator.yAt(projectile, 0.4 + it * 0.05) }
        assertTrue(positions.all { abs(it - projectile.targetY) <= 16.0 })
        assertTrue(positions.max() - positions.min() > 25.0)
    }

    @Test
    fun sharedFloatingKeepsFormationSpacingConstant() {
        val top = rock(228.0)
        val bottom = rock(508.0)
        repeat(100) { frame ->
            val age = 0.4 + frame * 0.05
            val spacing =
                ProjectileMovementCalculator.yAt(bottom, age) - ProjectileMovementCalculator.yAt(top, age)
            assertEquals(280.0, spacing, 0.000001)
        }
    }
}
