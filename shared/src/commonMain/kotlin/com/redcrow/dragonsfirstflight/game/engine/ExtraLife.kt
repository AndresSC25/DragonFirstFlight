package com.redcrow.dragonsfirstflight.game.engine

/** Represents a life pickup whose screen X is its fixed world X minus [GameSnapshot.extraLifeScrollX]. */
data class ExtraLife(
    val id: Int,
    val worldX: Double,
    val y: Double,
)
