package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal class ExtraLifeField(
    private val random: Random = Random.Default,
) {
    private val activeExtraLives = ArrayList<ExtraLife>()
    private var nextId = 0
    private var lastSeenObstacleId = -1
    private var extraLivesSpawned = 0
    private var secondsUntilNextAttempt = GameEngineConfig.EXTRA_LIFE_FIRST_ATTEMPT_SECONDS
    private var scrollX = 0.0

    val extraLives: List<ExtraLife>
        get() = activeExtraLives

    val currentScrollX: Double
        get() = scrollX

    fun reset() {
        activeExtraLives.clear()
        nextId = 0
        lastSeenObstacleId = -1
        extraLivesSpawned = 0
        secondsUntilNextAttempt = GameEngineConfig.EXTRA_LIFE_FIRST_ATTEMPT_SECONDS
        scrollX = 0.0
    }

    fun clear() {
        activeExtraLives.clear()
    }

    fun advance(
        elapsedSeconds: Double,
        distance: Double,
        leftBoundary: Double,
    ) {
        secondsUntilNextAttempt -= elapsedSeconds
        scrollX += distance
        activeExtraLives.removeAll {
            it.worldX - scrollX + GameEngineConfig.EXTRA_LIFE_SIZE < leftBoundary
        }
    }

    fun spawnForNewObstacles(
        obstacles: List<Obstacle>,
        rightBoundary: Double,
        zone: GameZone,
    ) {
        if (
            zone == GameZone.BOSS ||
            extraLivesSpawned >= GameEngineConfig.EXTRA_LIFE_MAX_PER_RUN ||
            secondsUntilNextAttempt > 0.0
        ) {
            return
        }

        for (obstacle in obstacles) {
            if (obstacle.id <= lastSeenObstacleId) continue
            lastSeenObstacleId = obstacle.id
            if (obstacle.x < rightBoundary - GameEngineConfig.EXTRA_LIFE_SPAWN_EDGE_TOLERANCE) continue
            if (random.nextDouble() > GameEngineConfig.extraLifeChance(zone)) continue

            val y =
                GameEngineConfig.extraLifeLanes
                    .filter { lane -> kotlin.math.abs(lane - obstacle.y) >= GameEngineConfig.EXTRA_LIFE_SAFE_LANE_DISTANCE }
                    .randomOrNull(random)
                    ?: return
            activeExtraLives +=
                ExtraLife(
                    id = nextId++,
                    worldX = obstacle.x + obstacle.width + GameEngineConfig.EXTRA_LIFE_OBSTACLE_GAP + scrollX,
                    y = y,
                )
            extraLivesSpawned++
            secondsUntilNextAttempt = GameEngineConfig.EXTRA_LIFE_MIN_INTERVAL_SECONDS
            return
        }
    }

    fun collect(
        dragonY: Double,
        canIncreaseLives: Boolean,
    ): Boolean {
        if (!canIncreaseLives) {
            return false
        }

        for (index in activeExtraLives.lastIndex downTo 0) {
            val extraLife = activeExtraLives[index]
            if (
                rectanglesOverlap(
                    firstX = GameEngineConfig.DRAGON_X,
                    firstY = dragonY,
                    firstWidth = GameEngineConfig.DRAGON_SIZE,
                    firstHeight = GameEngineConfig.DRAGON_SIZE,
                    secondX = extraLife.worldX - scrollX,
                    secondY = extraLife.y,
                    secondWidth = GameEngineConfig.EXTRA_LIFE_SIZE,
                    secondHeight = GameEngineConfig.EXTRA_LIFE_SIZE,
                )
            ) {
                activeExtraLives.removeAt(index)
                return true
            }
        }
        return false
    }

    fun screenX(extraLife: ExtraLife): Double = extraLife.worldX - scrollX
}
