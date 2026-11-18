package com.redcrow.dragonsfirstflight.game.engine

enum class GameDifficulty(
    val displayName: String,
    val description: String,
    private val obstacleSpeedMultiplier: Double,
    private val obstacleSeparationMultiplier: Double,
) {
    EASY(
        displayName = "Easy",
        description = "Classic",
        obstacleSpeedMultiplier = 1.0,
        obstacleSeparationMultiplier = 1.0,
    ),
    MEDIUM(
        displayName = "Medium",
        description = "Challenging",
        obstacleSpeedMultiplier = 1.15,
        obstacleSeparationMultiplier = 0.92,
    ),
    HARD(
        displayName = "Hard",
        description = "Intense",
        obstacleSpeedMultiplier = 1.30,
        obstacleSeparationMultiplier = 0.85,
    ),
    ;

    init {
        require(obstacleSpeedMultiplier.isFinite() && obstacleSpeedMultiplier > 0.0) {
            "The obstacle speed multiplier must be finite and greater than zero."
        }
        require(obstacleSeparationMultiplier.isFinite() && obstacleSeparationMultiplier > 0.0) {
            "The obstacle separation multiplier must be finite and greater than zero."
        }
    }

    fun obstacleSpeedAt(activeTimeSeconds: Double): Double =
        GameEngineConfig.obstacleSpeedAt(activeTimeSeconds) * obstacleSpeedMultiplier

    fun obstacleSeparationAt(activeTimeSeconds: Double): Double =
        GameEngineConfig.obstacleSeparationAt(activeTimeSeconds) * obstacleSeparationMultiplier
}
