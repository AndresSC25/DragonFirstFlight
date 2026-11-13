package com.redcrow.dragonsfirstflight.game.engine

internal data class CollisionBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
) {
    init {
        require(x.isFinite()) { "Collision x must be finite." }
        require(y.isFinite()) { "Collision y must be finite." }
        require(width.isFinite() && width > 0.0) {
            "Collision width must be finite and greater than zero."
        }
        require(height.isFinite() && height > 0.0) {
            "Collision height must be finite and greater than zero."
        }
    }
}
