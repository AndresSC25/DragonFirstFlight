package com.redcrow.dragonsfirstflight.game.engine

data class GameSnapshot(
    val dragonX: Double,
    val dragonY: Double,
    val dragonSize: Double,
    val obstacleX: Double,
    val obstacleY: Double,
    val obstacleSize: Double,
    val verticalVelocity: Double,
    val lives: Int,
    val points: Int,
    val status: GameStatus
) {
    val isRunning: Boolean
        get() = status == GameStatus.RUNNING
}
