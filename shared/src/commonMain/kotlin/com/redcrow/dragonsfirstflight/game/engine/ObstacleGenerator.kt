package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal class ObstacleGenerator(
    areaWidth: Double,
    private val random: Random = Random.Default,
    private var difficulty: GameDifficulty = GameDifficulty.EASY,
    initialZone: GameZone = GameZone.ZONE_ONE,
) {
    private val obstacleSpritePathsByZone =
        listOf(
            listOf(
                "sprites/obstacles/zone1/flying_bat.png",
                "sprites/obstacles/zone1/flying_crows.png",
                "sprites/obstacles/zone1/flying_sword.png",
                "sprites/obstacles/zone1/flaming_arrow.png",
            ),
            listOf(
                "sprites/obstacles/zone2/flying _skull_wisp.png",
                "sprites/obstacles/zone2/flying_cursed_book.png",
                "sprites/obstacles/zone2/flying_gargoyle.png",
                "sprites/obstacles/zone2/ghost_knight.png",
                "sprites/obstacles/zone2/winged_imp.png",
            ),
            listOf(
                "sprites/obstacles/zone3/fairy_sprite.png",
                "sprites/obstacles/zone3/flying_forest_monster.png",
                "sprites/obstacles/zone3/giant_hornet.png",
                "sprites/obstacles/zone3/wind_spirit.png",
            ),
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
    private val groundSpritePaths =
        mutableListOf<String>()
    private var currentZone: GameZone = GameZone.ZONE_ONE

    private var lastLane: Double? = null
    private var consecutiveLaneCount: Int = 0
    private var previousWasRush: Boolean = false
    private var previousWasGround: Boolean = false

    private val laneUsage =
        mutableMapOf<Double, Int>()

    private val patternSequencer =
        ObstaclePatternSequencer(random)

    init {
        reset(initialZone = initialZone)
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

        if (spawnIfEmpty && currentZone != GameZone.BOSS && obstacles.isEmpty()) {
            spawnObstacle(
                x = rightBoundary,
                activeTimeSeconds = currentZone.ordinal * GameEngineConfig.ZONE_DURATION_SECONDS,
            )
        }
    }

    fun update(
        delta: Double,
        activeTimeSeconds: Double,
        profileTimeSeconds: Double = activeTimeSeconds,
    ) {
        if (GameEngineConfig.zoneAt(profileTimeSeconds) == GameZone.BOSS) {
            beginZoneTransition(GameZone.BOSS)
            return
        }
        val obstacleSpeed =
            difficulty.obstacleSpeedAt(profileTimeSeconds)
        val obstacleSeparation =
            difficulty.obstacleSeparationAt(profileTimeSeconds)
        val zone =
            GameEngineConfig.zoneAt(profileTimeSeconds)

        if (zone != currentZone) {
            beginZoneTransition(zone)
        }

        for (index in obstacles.indices) {
            val obstacle = obstacles[index]
            val warningTimeConsumed =
                minOf(delta, obstacle.rushWarningRemainingSeconds)
            val movementDelta = delta - warningTimeConsumed
            val warningRemainingSeconds =
                (obstacle.rushWarningRemainingSeconds - delta).coerceAtLeast(0.0)

            val movedObstacle =
                obstacle.copy(
                    x =
                        obstacle.x -
                            obstacleSpeed *
                            obstacle.horizontalBehavior.speedMultiplier *
                            movementDelta,
                    rushWarningRemainingSeconds = warningRemainingSeconds,
                )

            obstacles[index] =
                movedObstacle.copy(
                    y =
                        ObstacleMovementCalculator.yAt(
                            obstacle = movedObstacle,
                            activeTimeSeconds = activeTimeSeconds,
                        ),
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
                activeTimeSeconds = profileTimeSeconds,
            )
        }
    }

    fun reset(
        difficulty: GameDifficulty = this.difficulty,
        initialZone: GameZone = GameZone.ZONE_ONE,
    ) {
        this.difficulty = difficulty
        obstacles.clear()
        nextId = 0
        spritePaths.clear()
        groundSpritePaths.clear()
        currentZone = initialZone
        lastLane = null
        consecutiveLaneCount = 0
        previousWasRush = false
        previousWasGround = false
        laneUsage.clear()
        patternSequencer.reset()
        if (initialZone != GameZone.BOSS) {
            spawnObstacle(
                x = rightBoundary - GameEngineConfig.INITIAL_OBSTACLE_MARGIN,
                activeTimeSeconds = initialZone.ordinal * GameEngineConfig.ZONE_DURATION_SECONDS,
            )
        }
    }

    val activeObstacles: List<Obstacle>
        get() = obstacles

    fun snapshot(): List<Obstacle> = obstacles.toList()

    fun beginZoneTransition(zone: GameZone) {
        obstacles.clear()
        spritePaths.clear()
        groundSpritePaths.clear()
        currentZone = zone
        lastLane = null
        consecutiveLaneCount = 0
        previousWasRush = false
        previousWasGround = false
        laneUsage.clear()
        patternSequencer.reset()
    }

    private fun spawnObstacle(
        x: Double,
        activeTimeSeconds: Double,
    ) {
        val shouldSpawnGroundObstacle =
            currentZone == GameZone.ZONE_ONE &&
                obstacles.isNotEmpty() &&
                !previousWasGround &&
                random.nextDouble() < groundObstacleProbability()

        val spritePath =
            if (shouldSpawnGroundObstacle) {
                if (groundSpritePaths.isEmpty()) {
                    groundSpritePaths += bottomOnlyObstaclePaths.shuffled(random)
                }
                groundSpritePaths.removeAt(0)
            } else {
                if (spritePaths.isEmpty()) {
                    val zoneIndex = currentZone.ordinal
                    spritePaths += obstacleSpritePathsByZone[zoneIndex].shuffled(random)
                }
                spritePaths.removeAt(0)
            }

        val singleLaneCandidates =
            obstacleLanes.filter { lane ->
                lane != lastLane || consecutiveLaneCount < 2
            }

        val leastUsedCount =
            singleLaneCandidates.minOf { candidate ->
                laneUsage[candidate] ?: 0
            }

        val balancedSingleLane =
            singleLaneCandidates
                .filter { candidate ->
                    (laneUsage[candidate] ?: 0) == leastUsedCount
                }.random(random)

        var patternStep: ObstaclePatternStep? = null
        val lane =
            when {
                spritePath in bottomOnlyObstaclePaths -> {
                    GameEngineConfig.OBSTACLE_BOTTOM_Y
                }

                obstacles.isEmpty() -> {
                    GameEngineConfig.OBSTACLE_Y
                }

                else -> {
                    patternSequencer
                        .nextStep(
                            zone = currentZone,
                            difficulty = difficulty,
                            singleLane = balancedSingleLane,
                        ).also { step -> patternStep = step }
                        .lane
                }
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

                "sprites/obstacles/zone2/flying_gargoyle.png" -> {
                    GameEngineConfig.ZONE_TWO_FLYING_GARGOYLE_SIZE
                }

                "sprites/obstacles/zone2/ghost_knight.png" -> {
                    GameEngineConfig.ZONE_TWO_GHOST_KNIGHT_SIZE
                }

                else -> {
                    GameEngineConfig.OBSTACLE_SIZE
                }
            }
        val isBottomOnly = spritePath in bottomOnlyObstaclePaths
        val horizontalBehavior =
            ObstacleHorizontalBehaviorSelector.select(
                difficulty = difficulty,
                zone = currentZone,
                isBottomOnly =
                    isBottomOnly ||
                        obstacles.isEmpty() ||
                        patternStep?.suppressRush == true,
                previousWasRush = previousWasRush,
                random = random,
            )
        val movement =
            if (horizontalBehavior.isRush) {
                ObstacleMovement.STATIC
            } else {
                ObstacleMovementSelector.select(
                    zone = currentZone,
                    isBottomOnly = isBottomOnly,
                    random = random,
                )
            }

        val patternSeparationMultiplier =
            patternStep?.separationMultiplier ?: 1.0
        val patternAdjustedX =
            if (obstacles.isNotEmpty()) {
                maxOf(
                    x,
                    obstacles.last().x +
                        difficulty.obstacleSeparationAt(activeTimeSeconds) *
                        patternSeparationMultiplier,
                )
            } else {
                x
            }

        val effectiveX =
            if (horizontalBehavior.isRush && obstacles.isNotEmpty()) {
                maxOf(
                    patternAdjustedX,
                    obstacles.last().x +
                        difficulty.obstacleSeparationAt(activeTimeSeconds) *
                        horizontalBehavior.separationMultiplier,
                )
            } else {
                patternAdjustedX
            }

        obstacles +=
            Obstacle(
                id = nextId++,
                x = effectiveX,
                y = lane,
                size = obstacleSize,
                spritePath = spritePath,
                baseY = lane,
                movement = movement,
                movementPhase = random.nextDouble() * 2.0 * kotlin.math.PI,
                horizontalBehavior = horizontalBehavior,
                rushWarningRemainingSeconds =
                    if (horizontalBehavior.isRush) {
                        GameEngineConfig.RUSH_WARNING_DURATION_SECONDS
                    } else {
                        0.0
                    },
            )
        previousWasRush = horizontalBehavior.isRush
        previousWasGround = isBottomOnly
    }

    private fun groundObstacleProbability(): Double =
        when (difficulty) {
            GameDifficulty.EASY -> 0.20
            GameDifficulty.MEDIUM -> 0.12
            GameDifficulty.HARD -> 0.08
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
