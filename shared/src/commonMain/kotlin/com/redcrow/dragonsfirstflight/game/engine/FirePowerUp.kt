package com.redcrow.dragonsfirstflight.game.engine

/** Represents the fireball pickup at a fixed world X, offset on screen by [GameSnapshot.firePowerUpScrollX]. */
data class FirePowerUp(
    val id: Int,
    val worldX: Double,
    val y: Double,
)
