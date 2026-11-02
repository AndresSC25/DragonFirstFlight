package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random

internal class ObstacleGenerator(
    areaWidth: Double,
    private val random: Random = Random.Default,
) {
    private val obstacleSpritePathsByZone =
        listOf(
            listOf(
                "sprites/obstacles/zone1/flying_bat.png",
                "sprites/obstacles/zone1/flying_crows.png",
                "sprites/obstacles/zone1/flying_sword.png",
                "sprites/obstacles/zone1/flaming_arrow.png",
                "sprites/obstacles/zone1/forest_mage.png",
                "sprites/obstacles/zone1/knight_sword.png",
            ),
            listOf(
                "sprites/obstacles/zone1/flying_bat.png",
                "sprites/obstacles/zone1/flying_crows.png",
            ),
            listOf(
                "sprites/obstacles/zone1/flying_bat.png",
                "sprites/obstacles/zone1/flying_crows.png",
            ),
        )
    private val laterZoneObstaclePaths =
        setOf(
            "sprites/obstacles/zone1/flying_bat.png",
            "sprites/obstacles/zone1/flying_crows.png",
        )

    private val obstacleLanes =
        listOf(
            GameEngineConfig.OBSTACLE_TOP_Y,
            GameEngineConfig.OBSTACLE_Y,
            GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y,
            GameEngineConfig.OBSTACLE_BOTTOM_Y,
        )

    private val bottomOnlyObstaclePaths =
        setOf(
            "sprites/obstacles/zone1/forest_mage.png",
            "sprites/obstacles/zone1/knight_sword.png",
        )

    private var leftBoundary: Double = 0.0
    private var rightBoundary: Double =
        validateRightBoundary(areaWidth)

    private var nextId: Int = 0

    private val obstacles =
        mutableListOf<Obstacle>()

    private val spritePaths =
        mutableListOf<String>()
    private var currentZone: GameZone = GameZone.ZONE_ONE

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
        spawnIfEmpty: Boolean = true,
    ) {
        validateBoundaries(
            left = newLeftBoundary,
            right = newRightBoundary,
        )

        leftBoundary = newLeftBoundary
        rightBoundary = newRightBoundary

        obstacles.removeAll { obstacle ->
            obstacle.x + obstacle.width < leftBoundary
        }

        if (spawnIfEmpty && obstacles.isEmpty()) {
            spawnObstacle(
                x = rightBoundary,
                activeTimeSeconds = 0.0,
            )
        }
    }

    fun update(
        delta: Double,
        activeTimeSeconds: Double,
    ) {
        val obstacleSpeed =
            GameEngineConfig.obstacleSpeedAt(activeTimeSeconds)
        val obstacleSeparation =
            GameEngineConfig.obstacleSeparationAt(activeTimeSeconds)
        val zone =
            GameEngineConfig.zoneAt(activeTimeSeconds)

        if (zone != currentZone) {
            beginZoneTransition(zone)
        }

        for (index in obstacles.indices) {
            val obstacle = obstacles[index]

            obstacles[index] =
                obstacle.copy(
                    x =
                        obstacle.x -
                            obstacleSpeed *
                            delta,
                )
        }

        obstacles.removeAll { obstacle ->
            obstacle.x + obstacle.width < leftBoundary
        }

        val lastObstacle =
            obstacles.lastOrNull()

        if (
            lastObstacle == null ||
            lastObstacle.x <=
            rightBoundary - obstacleSeparation
        ) {
            val nextObstacleX =
                lastObstacle?.let { obstacle ->
                    obstacle.x +
                        obstacleSeparation
                } ?: (
                    rightBoundary -
                        GameEngineConfig.INITIAL_OBSTACLE_MARGIN
                )

            spawnObstacle(
                x =
                    nextObstacleX.coerceAtLeast(
                        minimumValue =
                            rightBoundary -
                                GameEngineConfig.INITIAL_OBSTACLE_MARGIN,
                    ),
                activeTimeSeconds = activeTimeSeconds,
            )
        }
    }

    fun reset() {
        obstacles.clear()
        nextId = 0
        spritePaths.clear()
        currentZone = GameZone.ZONE_ONE
        lastLane = null
        consecutiveLaneCount = 0
        laneUsage.clear()
        spawnObstacle(
            x = rightBoundary - GameEngineConfig.INITIAL_OBSTACLE_MARGIN,
            activeTimeSeconds = 0.0,
        )
    }

    fun snapshot(): List<Obstacle> = obstacles.toList()

    fun beginZoneTransition(zone: GameZone) {
        obstacles.clear()
        spritePaths.clear()
        currentZone = zone
        lastLane = null
        consecutiveLaneCount = 0
        laneUsage.clear()
    }

    private fun spawnObstacle(
        x: Double,
        activeTimeSeconds: Double,
    ) {
        if (spritePaths.isEmpty()) {
            val zoneIndex = currentZone.ordinal
            spritePaths +=
                obstacleSpritePathsByZone[zoneIndex].shuffled(random)
            if (
                currentZone != GameZone.ZONE_ONE &&
                spritePaths.any { path ->
                    path !in laterZoneObstaclePaths
                }
            ) {
                error("Later zones may only generate crow and bat obstacles.")
            }
            if (
                obstacles.isEmpty() &&
                spritePaths.first() in bottomOnlyObstaclePaths
            ) {
                val firstNonBottomOnlyIndex =
                    spritePaths.indexOfFirst { path ->
                        path !in bottomOnlyObstaclePaths
                    }
                if (firstNonBottomOnlyIndex >= 0) {
                    val firstNonBottomOnlyPath =
                        spritePaths.removeAt(firstNonBottomOnlyIndex)
                    spritePaths.add(0, firstNonBottomOnlyPath)
                }
            }
        }

        val occupiedLanes =
            obstacles
                .filter { obstacle ->
                    abs(obstacle.x - x) <=
                        GameEngineConfig.obstacleSeparationAt(
                            activeTimeSeconds,
                        )
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

        val spritePath = spritePaths.first()
        val lane =
            if (spritePath in bottomOnlyObstaclePaths) {
                GameEngineConfig.OBSTACLE_BOTTOM_Y
            } else if (obstacles.isEmpty()) {
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

        val obstacleSize =
            when (spritePath) {
                "sprites/obstacles/zone1/forest_mage.png" -> {
                    GameEngineConfig.FOREST_MAGE_SIZE
                }

                "sprites/obstacles/zone1/knight_sword.png" -> {
                    GameEngineConfig.KNIGHT_SWORD_SIZE
                }

                else -> {
                    GameEngineConfig.OBSTACLE_SIZE
                }
            }
        obstacles +=
            Obstacle(
                id = nextId++,
                x = x,
                y = lane,
                size = obstacleSize,
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
