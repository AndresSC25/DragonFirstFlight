package com.redcrow.dragonsfirstflight.game.engine

data class CatapultSnapshot(
    val x: Double,
    val y: Double,
    val size: Double,
    val isEntering: Boolean,
    val frameIndex: Int = 0,
    val warningFrameIndex: Int? = null,
)
