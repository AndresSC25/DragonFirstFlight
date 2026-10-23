package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random

internal class ObstacleGenerator(
    areaWidth: Double,
    private val random: Random = Random.Default,
) {
    private val obstacleSpritePaths =
        listOf(
            "sprites/obstacles/zone1/broken_cart.png",
            "sprites/obstacles/zone1/broken_stone_column.png",
            "sprites/obstacles/zone1/hanging_banner.png",
            "sprites/obstacles/zone1/training_dummy.png",
            "sprites/obstacles/zone1/wooden_barrier.png",
            "sprites/obstacles/zone1/wooden_crates.png",
        )

    private val obstacleLanes =
        listOf(
            GameEngineConfig.OBSTACLE_TOP_Y,
            GameEngineConfig.OBSTACLE_Y,
            GameEngineConfig.OBSTACLE_BOTTOM_Y,
        )

    private var leftBoundary: Double = 0.0
    private var rightBoundary: Double =
        validateRightBoundary(areaWidth)

    private var nextId: Int = 0

    private val obstacles =
        mutableListOf<Obstacle>()

    private val spritePaths =
        mutableListOf<String>()

    private var lastLane: Double? = null
    private var consecutiveLaneCount: Int = 0

    private val laneUsage =
        mutableMapOf<Double, Int>()

    init {
        reset()
    }

    fun resize(
        newLeftBoundary: Double,
        newRightBoundary: Double,
    ) {
        validateBoundaries(
            left = newLeftBoundary,
            right = newRightBoundary,
        )

        leftBoundary = newLeftBoundary
        rightBoundary = newRightBoundary

        obstacles.removeAll { obstacle ->
            obstacle.x + obstacle.size < leftBoundary
        }

        if (obstacles.isEmpty()) {
            spawnObstacle(rightBoundary)
        }
    }

    fun update(delta: Double) {
        for (index in obstacles.indices) {
            val obstacle = obstacles[index]

            obstacles[index] =
                obstacle.copy(
                    x =
                        obstacle.x -
                            GameEngineConfig.OBSTACLE_SPEED *
                            delta,
                )
        }

        obstacles.removeAll { obstacle ->
            obstacle.x + obstacle.size < leftBoundary
        }

        val lastObstacle =
            obstacles.lastOrNull()

        if (
            lastObstacle == null ||
            lastObstacle.x <=
            rightBoundary -
            GameEngineConfig.MIN_OBSTACLE_SEPARATION
        ) {
            val nextObstacleX =
                lastObstacle?.let { obstacle ->
                    obstacle.x +
                        GameEngineConfig.MIN_OBSTACLE_SEPARATION
                } ?: rightBoundary

            spawnObstacle(
                x =
                    nextObstacleX.coerceAtLeast(
                        minimumValue = rightBoundary,
                    ),
            )
        }
    }

    fun reset() {
        obstacles.clear()
        nextId = 0
        spritePaths.clear()
        lastLane = null
        consecutiveLaneCount = 0
        laneUsage.clear()
        spawnObstacle(rightBoundary)
    }

    fun snapshot(): List<Obstacle> = obstacles.toList()

    private fun spawnObstacle(x: Double) {
        if (spritePaths.isEmpty()) {
            spritePaths +=
                obstacleSpritePaths.shuffled(random)
        }

        val occupiedLanes =
            obstacles
                .filter { obstacle ->
                    abs(obstacle.x - x) <=
                        GameEngineConfig.MIN_OBSTACLE_SEPARATION
                }.map { obstacle ->
                    obstacle.y
                }.toSet()

        val availableLanes =
            obstacleLanes.filter { lane ->
                lane !in occupiedLanes
            }

        val lanesWithoutConsecutiveRepeat =
            availableLanes.filter { lane ->
                lane != lastLane ||
                    consecutiveLaneCount < 2
            }

        val lane =
            if (nextId == 0) {
                GameEngineConfig.OBSTACLE_Y
            } else {
                val candidates =
                    when {
                        lanesWithoutConsecutiveRepeat.isNotEmpty() -> {
                            lanesWithoutConsecutiveRepeat
                        }

                        availableLanes.isNotEmpty() -> {
                            availableLanes
                        }

                        else -> {
                            obstacleLanes
                        }
                    }

                val leastUsedCount =
                    candidates.minOf { candidate ->
                        laneUsage[candidate] ?: 0
                    }

                candidates
                    .filter { candidate ->
                        (laneUsage[candidate] ?: 0) ==
                            leastUsedCount
                    }.random(random)
            }

        if (lane == lastLane) {
            consecutiveLaneCount += 1
        } else {
            lastLane = lane
            consecutiveLaneCount = 1
        }

        laneUsage[lane] =
            (laneUsage[lane] ?: 0) + 1

        obstacles +=
            Obstacle(
                id = nextId++,
                x = x,
                y = lane,
                size = GameEngineConfig.OBSTACLE_SIZE,
                spritePath = spritePaths.removeAt(0),
            )
    }

    private fun validateRightBoundary(width: Double): Double {
        validateBoundaries(
            left = 0.0,
            right = width,
        )

        return width
    }

    private fun validateBoundaries(
        left: Double,
        right: Double,
    ) {
        require(left.isFinite()) {
            "The left boundary must be finite."
        }

        require(right.isFinite()) {
            "The right boundary must be finite."
        }

        require(right > left) {
            "The right boundary must be greater than the left boundary."
        }

        require(
            right - left >
                GameEngineConfig.INITIAL_OBSTACLE_MARGIN,
        ) {
            "The game area width must be greater than " +
                GameEngineConfig.INITIAL_OBSTACLE_MARGIN +
                "."
        }
    }
}
