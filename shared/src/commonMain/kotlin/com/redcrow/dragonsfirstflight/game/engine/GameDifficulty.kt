package com.redcrow.dragonsfirstflight.game.engine

enum class GameDifficulty(
    val displayName: String,
    val description: String,
    private val obstacleSpeedMultiplier: Double,
    private val obstacleSeparationMultiplier: Double,
) {
    EASY(
        displayName = "Easy",
        description = "A calmer flight with slower and more separated obstacles.",
        obstacleSpeedMultiplier = 0.85,
        obstacleSeparationMultiplier = 1.15,
    ),
    MEDIUM(
        displayName = "Medium",
        description = "The balanced Dragon's First Flight experience.",
        obstacleSpeedMultiplier = 1.0,
        obstacleSeparationMultiplier = 1.0,
    ),
    HARD(
        displayName = "Hard",
        description = "A faster flight with less space between obstacles.",
        obstacleSpeedMultiplier = 1.15,
        obstacleSeparationMultiplier = 0.90,
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
