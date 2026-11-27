package com.redcrow.dragonsfirstflight.game.engine

/** Represents the rage pickup at a fixed world X, offset on screen by [GameSnapshot.ragePowerUpScrollX]. */
data class RagePowerUp(
    val id: Int,
    val worldX: Double,
    val y: Double,
)
