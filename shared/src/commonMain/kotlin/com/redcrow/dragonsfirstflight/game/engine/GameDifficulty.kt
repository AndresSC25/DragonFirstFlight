package com.redcrow.dragonsfirstflight.game.engine

/** Scales obstacle speed and spacing and selects the speed progression for Endless play. */
enum class GameDifficulty(
    val displayName: String,
    val description: String,
    private val obstacleSpeedMultiplier: Double,
    private val obstacleSeparationMultiplier: Double,
    val endless: EndlessDifficultyConfig,
) {
    EASY(
        displayName = "Easy",
        description = "Classic",
        obstacleSpeedMultiplier = 1.0,
        obstacleSeparationMultiplier = 1.0,
        endless = EndlessDifficultyConfig(speedIncreasePerSecond = 0.6, maximumSpeed = 360.0),
    ),
    MEDIUM(
        displayName = "Medium",
        description = "Challenging",
        obstacleSpeedMultiplier = 1.15,
        obstacleSeparationMultiplier = 0.92,
        endless = EndlessDifficultyConfig(speedIncreasePerSecond = 0.9, maximumSpeed = 414.0),
    ),
    HARD(
        displayName = "Hard",
        description = "Intense",
        obstacleSpeedMultiplier = 1.30,
        obstacleSeparationMultiplier = 0.85,
        endless = EndlessDifficultyConfig(speedIncreasePerSecond = 1.2, maximumSpeed = 468.0),
    ),
    ;

    init {
        require(obstacleSpeedMultiplier.isFinite() && obstacleSpeedMultiplier > 0.0) {
            "The obstacle speed multiplier must be finite and greater than zero."
        }
        require(obstacleSeparationMultiplier.isFinite() && obstacleSeparationMultiplier > 0.0) {
            "The obstacle separation multiplier must be finite and greater than zero."
        }
        require(endless.maximumSpeed >= GameEngineConfig.ZONE_THREE_MAX_OBSTACLE_SPEED * obstacleSpeedMultiplier) {
            "Endless maximum speed must accommodate the final normal stage."
        }
        require(
            endless.maximumSpeed * GameEngineConfig.MAX_DELTA_SECONDS <
                minOf(GameEngineConfig.COIN_SPAWN_EDGE_TOLERANCE, GameEngineConfig.EXTRA_LIFE_SPAWN_EDGE_TOLERANCE),
        ) {
            "Reward spawn tolerances must cover one frame at maximum Endless speed."
        }
    }

    /** Returns the difficulty-adjusted scrolling speed in pixels per second at the given active run time. */
    fun obstacleSpeedAt(activeTimeSeconds: Double): Double =
        GameEngineConfig.obstacleSpeedAt(activeTimeSeconds) * obstacleSpeedMultiplier

    /** Returns the zone's base obstacle spacing in pixels after applying the difficulty multiplier. */
    fun obstacleSeparationAt(activeTimeSeconds: Double): Double =
        GameEngineConfig.obstacleSeparationAt(activeTimeSeconds) * obstacleSeparationMultiplier

    /** Uses the selected zone's entry speed or the final forest speed after victory, scaled by difficulty. */
    fun endlessStartingSpeed(
        zone: GameZone,
        afterVictory: Boolean = false,
    ): Double {
        val baseSpeed =
            if (afterVictory) {
                GameEngineConfig.ZONE_THREE_MAX_OBSTACLE_SPEED
            } else {
                when (zone) {
                    GameZone.ZONE_ONE -> GameEngineConfig.OBSTACLE_SPEED
                    GameZone.ZONE_TWO -> GameEngineConfig.ZONE_TWO_OBSTACLE_SPEED
                    GameZone.ZONE_THREE, GameZone.BOSS -> GameEngineConfig.ZONE_THREE_OBSTACLE_SPEED
                }
            }
        return baseSpeed * obstacleSpeedMultiplier
    }
}
