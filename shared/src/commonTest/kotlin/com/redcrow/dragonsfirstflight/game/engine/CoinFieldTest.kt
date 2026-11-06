package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinFieldTest {
    private val rightBoundary = 1280.0

    private val lanes =
        listOf(
            GameEngineConfig.OBSTACLE_TOP_Y,
            GameEngineConfig.OBSTACLE_Y,
            GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y,
            GameEngineConfig.OBSTACLE_BOTTOM_Y,
        )

    private fun obstacle(
        id: Int = 0,
        x: Double = rightBoundary,
        y: Double = GameEngineConfig.OBSTACLE_Y,
        size: Double = GameEngineConfig.OBSTACLE_SIZE,
    ) = Obstacle(id = id, x = x, y = y, size = size, spritePath = "test.png")

    private fun field(seed: Int = 1) =
        CoinField(
            random = Random(seed),
        ).also { field ->
            field.advance(elapsedSeconds = 10.0, distance = 0.0, leftBoundary = 0.0)
        }

    private fun CoinField.spawn(vararg obstacles: Obstacle) =
        spawnForNewObstacles(
            obstacles = obstacles.toList(),
            rightBoundary = rightBoundary,
            rowIntervalSeconds = 0.0,
            fiveHundredChance = 0.0,
        )

    @Test
    fun spawnsPatternBesideObstacleAtRightEdge() {
        val field = field()

        field.spawn(obstacle())

        assertEquals(GameEngineConfig.COIN_PATTERN_LENGTH, field.coins.size)
        field.coins.forEachIndexed { index, coin ->
            assertEquals(rightBoundary + index * GameEngineConfig.COIN_SPACING, coin.worldX)
        }
    }

    @Test
    fun noCoinsForObstacleInsideVisibleArea() {
        val field = field()

        field.spawn(obstacle(x = rightBoundary - GameEngineConfig.INITIAL_OBSTACLE_MARGIN))

        assertTrue(field.coins.isEmpty())
    }

    @Test
    fun eachObstacleIsHandledOnce() {
        val field = field()
        val obstacle = obstacle()

        field.spawn(obstacle)
        val countAfterFirstCall = field.coins.size
        field.spawn(obstacle)

        assertTrue(countAfterFirstCall > 0)
        assertEquals(countAfterFirstCall, field.coins.size)
    }

    @Test
    fun coinsNeverOverlapObstacleAndAreCollectibleWhileClearingIt() {
        val sway = 36.0
        val cases =
            lanes.flatMap { lane ->
                listOf(
                    obstacle(y = lane),
                    obstacle(y = lane, size = GameEngineConfig.ZONE_TWO_FLYING_GARGOYLE_SIZE),
                )
            } + obstacle(y = GameEngineConfig.OBSTACLE_BOTTOM_Y, size = GameEngineConfig.FOREST_MAGE_SIZE)

        for (laneObstacle in cases) {
            for (offset in listOf(-sway, 0.0, sway)) {
                val obstacle = laneObstacle.copy(y = laneObstacle.y + offset)
                var spawned = 0
                for (seed in 0 until 200) {
                    val field = field(seed)
                    field.spawn(obstacle)
                    if (field.coins.isEmpty()) continue
                    spawned += 1

                    for (coin in field.coins) {
                        val overlaps =
                            rectanglesOverlap(
                                firstX = coin.worldX,
                                firstY = coin.y,
                                firstWidth = GameEngineConfig.COIN_SIZE,
                                firstHeight = GameEngineConfig.COIN_SIZE,
                                secondX = obstacle.x,
                                secondY = laneObstacle.y - sway,
                                secondWidth = obstacle.width,
                                secondHeight = obstacle.height + 2 * sway,
                            )
                        assertTrue(!overlaps, "coin overlaps obstacle $obstacle with seed $seed")
                    }

                    assertTrue(
                        isCollectibleWhileClearing(field.coins, laneObstacle),
                        "no safe dragon height collects the coins of $obstacle with seed $seed",
                    )
                }
                assertTrue(spawned > 0, "no pattern ever spawned for $obstacle")
            }
        }
    }

    @Test
    fun advanceDropsCoinsPastLeftBoundaryAndKeepsWorldX() {
        val field = field()
        field.spawn(obstacle())
        val lastWorldX = field.coins.last().worldX

        field.advance(elapsedSeconds = 0.0, distance = 1400.0, leftBoundary = 0.0)

        assertEquals(1400.0, field.scrollX)
        assertEquals(1, field.coins.size)
        assertEquals(lastWorldX, field.coins.single().worldX)
    }

    @Test
    fun collectReturnsValueOnceThenZero() {
        val field = field()
        field.spawn(obstacle())
        // Only the first coin ends up over the dragon; the second one starts at its right edge.
        field.advance(elapsedSeconds = 0.0, distance = rightBoundary - 172.0, leftBoundary = 0.0)
        val dragonY = field.coins.first().y + GameEngineConfig.COIN_SIZE / 2 - GameEngineConfig.DRAGON_SIZE / 2

        assertEquals(100, field.collect(dragonY))
        assertEquals(0, field.collect(dragonY))
        assertEquals(GameEngineConfig.COIN_PATTERN_LENGTH - 1, field.coins.size)
    }

    @Test
    fun rowsFollowTimeBasedRate() {
        val field = CoinField(Random(1))
        repeat(600) { second ->
            field.advance(elapsedSeconds = 1.0, distance = 0.0, leftBoundary = 0.0)
            field.spawnForNewObstacles(
                obstacles = listOf(obstacle(id = second, y = lanes[second % lanes.size])),
                rightBoundary = rightBoundary,
                rowIntervalSeconds = 5.0,
                fiveHundredChance = 0.0,
            )
        }

        val rows = field.coins.size / GameEngineConfig.COIN_PATTERN_LENGTH
        assertTrue(rows in 96..144, "Expected about 120 rows, got $rows")
    }

    private fun isCollectibleWhileClearing(
        coins: List<Coin>,
        obstacle: Obstacle,
    ): Boolean {
        var dragonTop = GameEngineConfig.DRAGON_MIN_Y
        while (dragonTop <= GameEngineConfig.DRAGON_MAX_Y) {
            val dragonBottom = dragonTop + GameEngineConfig.DRAGON_SIZE
            val clearsObstacle =
                dragonBottom <= obstacle.y - 36.0 || dragonTop >= obstacle.y + obstacle.height + 36.0
            val touchesEveryCoin =
                coins.all { coin ->
                    coin.y < dragonBottom && coin.y + GameEngineConfig.COIN_SIZE > dragonTop
                }
            if (clearsObstacle && touchesEveryCoin) {
                return true
            }
            dragonTop += 1.0
        }
        return false
    }
}
