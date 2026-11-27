package com.redcrow.dragonsfirstflight.game.engine

/** Exposes flight, encounter and feedback state to the views and audio director for a single update. */
data class GameSnapshot(
    val dragonX: Double,
    val dragonY: Double,
    val dragonSize: Double,
    val obstacles: List<Obstacle>,
    /** Identifies obstacles already hit or destroyed so their sprites can be hidden. */
    val destroyedObstacleIds: Set<Int>,
    /** Vertical speed in game pixels per second, positive downward. */
    val verticalVelocity: Double,
    val lives: Int,
    val points: Int,
    val isImpactFeedbackActive: Boolean,
    val impactObstacleId: Int?,
    /** Indicates protection from a recent hit or rage. Shield protection is reported separately by [isShieldActive]. */
    val isInvulnerable: Boolean,
    val isInvulnerabilityBlinkDim: Boolean,
    val zone: GameZone,
    val isZoneTransitioning: Boolean,
    /** Fraction of the zone transition completed, from zero to one. */
    val zoneTransitionProgress: Double,
    val status: GameStatus,
    val coins: List<Coin>,
    /** Subtract this offset in pixels from each coin's world X to get its screen X. */
    val coinScrollX: Double,
    /** Total coin value collected this run, in colones. */
    val runCoins: Int,
    val extraLives: List<ExtraLife> = emptyList(),
    /** Subtract this offset in pixels from each extra life's world X to get its screen X. */
    val extraLifeScrollX: Double = 0.0,
    val firePowerUps: List<FirePowerUp> = emptyList(),
    /** Subtract this offset in pixels from each fire pickup's world X to get its screen X. */
    val firePowerUpScrollX: Double = 0.0,
    val isFirePowerActive: Boolean = false,
    val firePowerRemainingSeconds: Double = 0.0,
    val fireBalls: List<Projectile> = emptyList(),
    val shieldPowerUps: List<ShieldPowerUp> = emptyList(),
    /** Subtract this offset in pixels from each shield pickup's world X to get its screen X. */
    val shieldPowerUpScrollX: Double = 0.0,
    val isShieldActive: Boolean = false,
    val shieldRemainingSeconds: Double = 0.0,
    val ragePowerUps: List<RagePowerUp> = emptyList(),
    /** Subtract this offset in pixels from each rage pickup's world X to get its screen X. */
    val ragePowerUpScrollX: Double = 0.0,
    val isRageActive: Boolean = false,
    val rageRemainingSeconds: Double = 0.0,
    val catapult: CatapultSnapshot? = null,
    val projectiles: List<Projectile> = emptyList(),
    val impactPosition: ImpactPosition? = null,
    val rockImpacts: List<RockImpact> = emptyList(),
    val mode: GameMode = GameMode.NORMAL,
    val endlessElapsedSeconds: Double = 0.0,
    /** Current obstacle scrolling speed in pixels per second, including the rage multiplier. */
    val obstacleSpeed: Double = 0.0,
    /** Counts hits that cost a life, allowing feedback to distinguish damage from harmless collisions. */
    val damageCount: Int = 0,
    val extraLivesCollected: Int = 0,
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
