package com.redcrow.dragonsfirstflight.game.engine

data class Obstacle(
    val id: Int,
    val x: Double,
    val y: Double,
    val size: Double,
    val spritePath: String,
    val width: Double = size,
    val height: Double = size,
    val baseY: Double = y,
    internal val movement: ObstacleMovement = ObstacleMovement.STATIC,
    internal val movementPhase: Double = 0.0,
) {
    init {
        require(id >= 0) {
            "Obstacle id must not be negative."
        }
        require(x.isFinite()) {
            "Obstacle x position must be finite."
        }
        require(y.isFinite()) {
            "Obstacle y position must be finite."
        }
        require(baseY.isFinite()) {
            "Obstacle base y position must be finite."
        }
        require(size > 0.0 && size.isFinite()) {
            "Obstacle size must be finite and greater than zero."
        }
        require(width > 0.0 && width.isFinite()) {
            "Obstacle width must be finite and greater than zero."
        }
        require(height > 0.0 && height.isFinite()) {
            "Obstacle height must be finite and greater than zero."
        }
        require(spritePath.isNotBlank()) {
            "Obstacle sprite path must not be blank."
        }
        require(movementPhase.isFinite()) {
            "Obstacle movement phase must be finite."
        }
    }
}
