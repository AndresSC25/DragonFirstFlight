package com.redcrow.dragonsfirstflight.game.engine

data class Projectile(
    val id: Int,
    val x: Double,
    val y: Double,
    val size: Double,
    val velocityX: Double,
    val velocityY: Double,
    val ageSeconds: Double = 0.0,
    val baseY: Double = y,
    val targetY: Double = y,
    val formationDistance: Double = 0.0,
    val floatAmplitude: Double = 0.0,
)
