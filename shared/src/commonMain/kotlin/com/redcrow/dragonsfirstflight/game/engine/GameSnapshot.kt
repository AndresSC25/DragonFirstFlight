package com.redcrow.dragonsfirstflight.game.engine

data class GameSnapshot(
    val dragonX: Double,
    val dragonY: Double,
    val dragonSize: Double,
    val obstacles: List<Obstacle>,
    val verticalVelocity: Double,
    val lives: Int,
    val points: Int,
    val isImpactFeedbackActive: Boolean,
    val isInvulnerable: Boolean,
    val isInvulnerabilityBlinkDim: Boolean,
    val status: GameStatus
) {
    val firstObstacle: Obstacle?
        get() = obstacles.firstOrNull()

    val obstacleX: Double
        get() = firstObstacle?.x ?: Double.POSITIVE_INFINITY

    val obstacleY: Double
        get() = firstObstacle?.y ?: GameEngineConfig.OBSTACLE_Y

    val obstacleSize: Double
        get() = firstObstacle?.size ?: GameEngineConfig.OBSTACLE_SIZE

    val isRunning: Boolean
        get() = status == GameStatus.RUNNING
}
