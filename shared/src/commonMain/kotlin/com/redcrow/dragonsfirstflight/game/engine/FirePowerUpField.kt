package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal class FirePowerUpField(
    private val random: Random = Random.Default,
) {
    private val activePowerUps = ArrayList<FirePowerUp>()
    private var nextId = 0
    private var lastSeenObstacleId = -1
    private var spawnedInNormalZone = false
    private var endlessWindowElapsedSeconds = 0.0
    private var secondsUntilNextAttempt = GameEngineConfig.FIRE_POWER_UP_FIRST_ATTEMPT_SECONDS
    private var scrollX = 0.0

    val powerUps: List<FirePowerUp>
        get() = activePowerUps

    val currentScrollX: Double
        get() = scrollX

    fun reset() {
        activePowerUps.clear()
        nextId = 0
        lastSeenObstacleId = -1
        spawnedInNormalZone = false
        endlessWindowElapsedSeconds = 0.0
        secondsUntilNextAttempt = GameEngineConfig.FIRE_POWER_UP_FIRST_ATTEMPT_SECONDS
        scrollX = 0.0
    }

    fun clear() {
        activePowerUps.clear()
    }

    fun advance(
        elapsedSeconds: Double,
        distance: Double,
        leftBoundary: Double,
        isEndless: Boolean,
    ) {
        if (isEndless) {
            endlessWindowElapsedSeconds += elapsedSeconds
            if (endlessWindowElapsedSeconds >= GameEngineConfig.ENDLESS_FIRE_POWER_UP_WINDOW_SECONDS) {
                endlessWindowElapsedSeconds %= GameEngineConfig.ENDLESS_FIRE_POWER_UP_WINDOW_SECONDS
            }
        }
        secondsUntilNextAttempt -= elapsedSeconds
        scrollX += distance
        activePowerUps.removeAll {
            it.worldX - scrollX + GameEngineConfig.FIRE_POWER_UP_SIZE < leftBoundary
        }
    }

    fun spawnForNewObstacles(
        obstacles: List<Obstacle>,
        rightBoundary: Double,
        zone: GameZone,
        isEndless: Boolean,
        shieldPowerUpActive: Boolean = false,
        shieldPowerUpsPresent: Boolean = false,
    ) {
        if (shieldPowerUpActive || shieldPowerUpsPresent || activePowerUps.isNotEmpty()) return
        if (secondsUntilNextAttempt > 0.0) return
        if (!isEndless && (zone != GameZone.ZONE_ONE || spawnedInNormalZone)) return
        if (isEndless && random.nextDouble() >= 0.08) {
            for (obstacle in obstacles) {
                if (obstacle.id > lastSeenObstacleId) lastSeenObstacleId = obstacle.id
            }
            return
        }

        for (obstacle in obstacles) {
            if (obstacle.id <= lastSeenObstacleId) continue
            lastSeenObstacleId = obstacle.id
            if (obstacle.x < rightBoundary - GameEngineConfig.FIRE_POWER_UP_SPAWN_EDGE_TOLERANCE) continue

            val y =
                GameEngineConfig.extraLifeLanes
                    .filter { lane ->
                        kotlin.math.abs(lane - obstacle.y) >=
                            GameEngineConfig.FIRE_POWER_UP_SAFE_LANE_DISTANCE
                    }.randomOrNull(random)
                    ?: return
            activePowerUps +=
                FirePowerUp(
                    id = nextId++,
                    worldX = obstacle.x + obstacle.width + GameEngineConfig.FIRE_POWER_UP_OBSTACLE_GAP + scrollX,
                    y = y,
                )
            if (!isEndless) spawnedInNormalZone = true
            secondsUntilNextAttempt = GameEngineConfig.FIRE_POWER_UP_MIN_INTERVAL_SECONDS
            return
        }
    }

    fun collect(dragonY: Double): Boolean {
        for (index in activePowerUps.lastIndex downTo 0) {
            val powerUp = activePowerUps[index]
            if (
                rectanglesOverlap(
                    firstX = GameEngineConfig.DRAGON_X,
                    firstY = dragonY,
                    firstWidth = GameEngineConfig.DRAGON_SIZE,
                    firstHeight = GameEngineConfig.DRAGON_SIZE,
                    secondX = powerUp.worldX - scrollX,
                    secondY = powerUp.y,
                    secondWidth = GameEngineConfig.FIRE_POWER_UP_SIZE,
                    secondHeight = GameEngineConfig.FIRE_POWER_UP_SIZE,
                )
            ) {
                activePowerUps.removeAt(index)
                return true
            }
        }
        return false
    }
}
