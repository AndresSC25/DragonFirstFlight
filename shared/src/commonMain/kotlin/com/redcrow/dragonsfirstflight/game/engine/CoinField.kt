package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random

/**
 * Owns the coins of the current run. Coins are fixed in world space and a single [scrollX] offset
 * advances with the obstacles, so scrolling never touches individual coins.
 *
 * A due coin row is placed beside a newly spawned obstacle at a height the dragon can hold to both
 * collect the coins and clear the obstacle, so coins never overlap an obstacle nor lure the dragon
 * into one.
 */
internal class CoinField(
    private val random: Random = Random.Default,
) {
    private val activeCoins = ArrayList<Coin>()

    val coins: List<Coin>
        get() = activeCoins

    var scrollX: Double = 0.0
        private set

    private var lastSeenObstacleId: Int = -1
    private var secondsUntilNextRow: Double = GameEngineConfig.coinRowIntervalAt(GameZone.ZONE_ONE) * 0.5

    fun reset() {
        activeCoins.clear()
        scrollX = 0.0
        lastSeenObstacleId = -1
        secondsUntilNextRow = GameEngineConfig.coinRowIntervalAt(GameZone.ZONE_ONE) * 0.5
    }

    /** Drops the coins only. Obstacle ids keep increasing across zone transitions. */
    fun clear() {
        activeCoins.clear()
    }

    fun advance(
        elapsedSeconds: Double,
        distance: Double,
        leftBoundary: Double,
    ) {
        secondsUntilNextRow -= elapsedSeconds
        scrollX += distance
        for (index in activeCoins.lastIndex downTo 0) {
            val coin = activeCoins[index]
            if (coin.worldX - scrollX + GameEngineConfig.COIN_SIZE < leftBoundary) {
                activeCoins.removeAt(index)
            }
        }
    }

    fun spawnForNewObstacles(
        obstacles: List<Obstacle>,
        rightBoundary: Double,
        rowIntervalSeconds: Double,
        fiveHundredChance: Double,
    ) {
        for (obstacle in obstacles) {
            if (obstacle.id <= lastSeenObstacleId) {
                continue
            }
            lastSeenObstacleId = obstacle.id

            // Obstacles placed inside the visible area get no coins, so coins never pop in on screen.
            if (obstacle.x < rightBoundary - GameEngineConfig.COIN_SPAWN_EDGE_TOLERANCE) {
                continue
            }
            if (secondsUntilNextRow <= 0.0 && trySpawnPattern(obstacle, obstacles, fiveHundredChance)) {
                val jitter = GameEngineConfig.COIN_ROW_INTERVAL_JITTER
                secondsUntilNextRow = rowIntervalSeconds * (1 - jitter + 2 * jitter * random.nextDouble())
            }
        }
    }

    fun collect(dragonY: Double): Int {
        var collected = 0
        for (index in activeCoins.lastIndex downTo 0) {
            val coin = activeCoins[index]
            val touchesDragon =
                rectanglesOverlap(
                    firstX = GameEngineConfig.DRAGON_X,
                    firstY = dragonY,
                    firstWidth = GameEngineConfig.DRAGON_SIZE,
                    firstHeight = GameEngineConfig.DRAGON_SIZE,
                    secondX = coin.worldX - scrollX,
                    secondY = coin.y,
                    secondWidth = GameEngineConfig.COIN_SIZE,
                    secondHeight = GameEngineConfig.COIN_SIZE,
                )
            if (touchesDragon) {
                collected += coin.value.colones
                activeCoins.removeAt(index)
            }
        }
        return collected
    }

    private fun trySpawnPattern(
        obstacle: Obstacle,
        obstacles: List<Obstacle>,
        fiveHundredChance: Double,
    ): Boolean {
        val pattern = PATTERNS.random(random)
        val rowCenterY =
            laneRowCenters
                .filter { centerY -> isValidRow(centerY, pattern, obstacle, obstacles) }
                .randomOrNull(random) ?: return false

        for (index in pattern.indices) {
            val isMiddle = index == GameEngineConfig.COIN_PATTERN_LENGTH / 2
            val value =
                if (isMiddle && random.nextDouble() < fiveHundredChance) {
                    CoinValue.FIVE_HUNDRED
                } else {
                    CoinValue.HUNDRED
                }
            activeCoins +=
                Coin(
                    worldX = obstacle.x + index * GameEngineConfig.COIN_SPACING + scrollX,
                    y = coinTopY(rowCenterY, pattern[index]),
                    value = value,
                )
        }
        return true
    }

    private fun isValidRow(
        rowCenterY: Double,
        pattern: List<Double>,
        obstacle: Obstacle,
        obstacles: List<Obstacle>,
    ): Boolean {
        val padding = GameEngineConfig.COIN_OBSTACLE_PADDING
        val verticalPadding = padding + GameEngineConfig.COIN_OBSTACLE_SWAY_CLEARANCE
        val laneY = nearestLaneY(obstacle.y)
        val dragonTop = rowCenterY - GameEngineConfig.DRAGON_SIZE / 2
        val dragonBottom = dragonTop + GameEngineConfig.DRAGON_SIZE

        // Holding this height must also clear the obstacle that the row sits beside.
        val clearsObstacle =
            dragonBottom <= laneY - verticalPadding ||
                dragonTop >= laneY + obstacle.height + verticalPadding
        if (!clearsObstacle) {
            return false
        }

        for (index in pattern.indices) {
            val coinX = obstacle.x + index * GameEngineConfig.COIN_SPACING
            val coinY = coinTopY(rowCenterY, pattern[index])
            for (other in obstacles) {
                val overlaps =
                    rectanglesOverlap(
                        firstX = coinX,
                        firstY = coinY,
                        firstWidth = GameEngineConfig.COIN_SIZE,
                        firstHeight = GameEngineConfig.COIN_SIZE,
                        secondX = other.x - padding,
                        secondY = nearestLaneY(other.y) - verticalPadding,
                        secondWidth = other.width + 2 * padding,
                        secondHeight = other.height + 2 * verticalPadding,
                    )
                if (overlaps) {
                    return false
                }
            }
        }
        return true
    }

    private fun nearestLaneY(y: Double): Double = obstacleLanes.minBy { lane -> abs(lane - y) }

    private fun coinTopY(
        rowCenterY: Double,
        offsetY: Double,
    ): Double = rowCenterY + offsetY - GameEngineConfig.COIN_SIZE / 2

    private companion object {
        // Vertical offset of each coin relative to the row centre: line, arc, rise and fall.
        val PATTERNS: List<List<Double>> =
            listOf(
                listOf(0.0, 0.0, 0.0),
                listOf(0.0, -GameEngineConfig.COIN_PATTERN_STEP_Y, 0.0),
                listOf(GameEngineConfig.COIN_PATTERN_STEP_Y, 0.0, -GameEngineConfig.COIN_PATTERN_STEP_Y),
                listOf(-GameEngineConfig.COIN_PATTERN_STEP_Y, 0.0, GameEngineConfig.COIN_PATTERN_STEP_Y),
            )

        val obstacleLanes: List<Double> =
            listOf(
                GameEngineConfig.OBSTACLE_TOP_Y,
                GameEngineConfig.OBSTACLE_Y,
                GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y,
                GameEngineConfig.OBSTACLE_BOTTOM_Y,
            )

        // Row centre per obstacle lane: the dragon height that is level with an obstacle in that lane.
        val laneRowCenters: List<Double> =
            obstacleLanes.map { lane ->
                val dragonTop =
                    (lane + GameEngineConfig.OBSTACLE_SIZE / 2 - GameEngineConfig.DRAGON_SIZE / 2)
                        .coerceIn(GameEngineConfig.DRAGON_MIN_Y, GameEngineConfig.DRAGON_MAX_Y)
                dragonTop + GameEngineConfig.DRAGON_SIZE / 2
            }
    }
}
