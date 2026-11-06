package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineCoinTest {
    private val width = 1280.0

    private fun newEngine(seed: Int): GameEngine {
        val engine = GameEngine(gameAreaWidth = width, random = Random(seed))
        engine.resizeGameArea(0.0, width)
        return engine
    }

    @Test
    fun coinsNeverOverlapObstaclesAndRunCoinsOnlyGrowInHundreds() {
        var totalCollected = 0
        var totalFrames = 0

        for (seed in 0 until 8) {
            val engine = newEngine(seed)
            var previousRunCoins = 0
            var frame = 0
            while (engine.isRunning && frame < 1400) {
                if (frame % 6 == 0) engine.flap()
                engine.update(elapsedSeconds = 0.05)
                frame += 1

                val snapshot = engine.snapshot()
                for (coin in snapshot.coins) {
                    for (obstacle in snapshot.obstacles) {
                        val overlaps =
                            rectanglesOverlap(
                                firstX = coin.worldX - snapshot.coinScrollX,
                                firstY = coin.y,
                                firstWidth = GameEngineConfig.COIN_SIZE,
                                firstHeight = GameEngineConfig.COIN_SIZE,
                                secondX = obstacle.x,
                                secondY = obstacle.y,
                                secondWidth = obstacle.width,
                                secondHeight = obstacle.height,
                            )
                        assertTrue(!overlaps, "coin $coin overlaps $obstacle (seed $seed, frame $frame)")
                    }
                }
                assertTrue(snapshot.runCoins >= previousRunCoins)
                assertEquals(0, snapshot.runCoins % 100)
                assertEquals(engine.runCoins, snapshot.runCoins)
                previousRunCoins = snapshot.runCoins
            }
            totalCollected += previousRunCoins
            totalFrames += frame
        }

        assertTrue(totalFrames > 0)
        assertTrue(totalCollected > 0, "no coin was collected in any seeded run")
    }

    @Test
    fun restartResetsCoinsScrollAndRunCoins() {
        val engine = newEngine(seed = 0)
        var frame = 0
        while (engine.isRunning && frame < 400) {
            if (frame % 6 == 0) engine.flap()
            engine.update(elapsedSeconds = 0.05)
            frame += 1
        }
        assertTrue(engine.snapshot().coinScrollX > 0.0)

        engine.restart()

        val snapshot = engine.snapshot()
        assertTrue(snapshot.coins.isEmpty())
        assertEquals(0.0, snapshot.coinScrollX)
        assertEquals(0, snapshot.runCoins)
        assertEquals(0, engine.runCoins)
    }
}
