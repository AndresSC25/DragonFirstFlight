package com.redcrow.dragonsfirstflight.game.engine

/** Represents the shield pickup at a fixed world X, offset on screen by [GameSnapshot.shieldPowerUpScrollX]. */
data class ShieldPowerUp(
    val id: Int,
    val worldX: Double,
    val y: Double,
)
