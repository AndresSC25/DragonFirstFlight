package com.redcrow.dragonsfirstflight.game.engine

data class Obstacle(
    val id: Int,
    val x: Double,
    val y: Double,
    val size: Double,
    val spritePath: String,
    val width: Double = size,
    val height: Double = size
)
