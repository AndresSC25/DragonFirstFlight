package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal class ObstacleGenerator(
    areaWidth: Double,
    private val random: Random = Random.Default
) {
    private val obstacleSpritePaths = listOf(
        "sprites/obstacles/zone1/broken_cart.png",
        "sprites/obstacles/zone1/broken_stone_column.png",
        "sprites/obstacles/zone1/hanging_banner.png",
        "sprites/obstacles/zone1/training_dummy.png",
        "sprites/obstacles/zone1/wooden_barrier.png",
        "sprites/obstacles/zone1/wooden_crates.png"
    )

    private val obstacleLanes = listOf(
        GameEngineConfig.OBSTACLE_TOP_Y,
        GameEngineConfig.OBSTACLE_Y,
        GameEngineConfig.OBSTACLE_BOTTOM_Y
    )

    private var nextId: Int = 0
    private var areaWidth: Double = validateAreaWidth(areaWidth)
    private val obstacles = mutableListOf<Obstacle>()
    private val spritePaths = mutableListOf<String>()
    private var lastLane: Double? = null
    private var consecutiveLaneCount: Int = 0
    private val laneUsage = mutableMapOf<Double, Int>()

    init {
        reset()
    }

    fun resize(newWidth: Double) {
        areaWidth = validateAreaWidth(newWidth)
        while (
            obstacles.isNotEmpty() &&
            obstacles.first().x + GameEngineConfig.OBSTACLE_SIZE < 0.0
        ) {
            obstacles.removeAt(0)
        }
        if (obstacles.isEmpty()) {
            spawnObstacle(areaWidth - GameEngineConfig.INITIAL_OBSTACLE_MARGIN)
        }
    }

    fun update(delta: Double) {
        for (index in obstacles.indices) {
            val obstacle = obstacles[index]
            obstacles[index] = obstacle.copy(
                x = obstacle.x - GameEngineConfig.OBSTACLE_SPEED * delta
            )
        }

        obstacles.removeAll {
            it.x + it.size < 0.0
        }

        val lastObstacle = obstacles.lastOrNull()
        if (
            lastObstacle == null ||
            lastObstacle.x <=
                areaWidth - GameEngineConfig.MIN_OBSTACLE_SEPARATION
        ) {
            spawnObstacle(
                x = lastObstacle?.let {
                    it.x + GameEngineConfig.MIN_OBSTACLE_SEPARATION
                } ?: (areaWidth - GameEngineConfig.INITIAL_OBSTACLE_MARGIN)
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
        spawnObstacle(areaWidth - GameEngineConfig.INITIAL_OBSTACLE_MARGIN)
    }

    fun snapshot(): List<Obstacle> = obstacles.toList()

    private fun spawnObstacle(x: Double) {
        if (spritePaths.isEmpty()) {
            spritePaths += obstacleSpritePaths.shuffled(random)
        }

        val occupiedLanes = obstacles
            .filter {
                kotlin.math.abs(it.x - x) <=
                    GameEngineConfig.MIN_OBSTACLE_SEPARATION
            }
            .map { it.y }
            .toSet()
        val availableLanes = obstacleLanes.filter {
            it !in occupiedLanes
        }
        val lanesWithoutConsecutiveRepeat = availableLanes.filter {
            it != lastLane || consecutiveLaneCount < 2
        }
        val lane = if (nextId == 0) {
            GameEngineConfig.OBSTACLE_Y
        } else {
            val candidates = when {
                lanesWithoutConsecutiveRepeat.isNotEmpty() ->
                    lanesWithoutConsecutiveRepeat

                availableLanes.isNotEmpty() -> availableLanes

                else -> obstacleLanes
            }
            val leastUsedCount = candidates.minOf {
                laneUsage[it] ?: 0
            }
            candidates.filter {
                (laneUsage[it] ?: 0) == leastUsedCount
            }.random(random)
        }
        if (lane == lastLane) {
            consecutiveLaneCount += 1
        } else {
            lastLane = lane
            consecutiveLaneCount = 1
        }
        laneUsage[lane] = (laneUsage[lane] ?: 0) + 1

        obstacles += Obstacle(
            id = nextId++,
            x = x,
            y = lane,
            size = GameEngineConfig.OBSTACLE_SIZE,
            spritePath = spritePaths.removeAt(0)
        )
    }

    private fun validateAreaWidth(width: Double): Double {
        require(width.isFinite()) {
            "The game area width must be finite."
        }
        require(width > GameEngineConfig.INITIAL_OBSTACLE_MARGIN) {
            "The game area width must be greater than " +
                GameEngineConfig.INITIAL_OBSTACLE_MARGIN +
                "."
        }
        return width
    }
}
