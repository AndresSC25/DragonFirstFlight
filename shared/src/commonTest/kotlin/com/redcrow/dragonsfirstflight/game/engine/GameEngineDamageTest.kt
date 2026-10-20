package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineDamageTest {
    @Test
    fun overlappingObstacleEmitsDamageOnceUntilNextObstacle() {
        val eventObstacleXs = mutableListOf<Double>()
        lateinit var engine: GameEngine
        engine = GameEngine(gameAreaWidth = 240.0) {
            eventObstacleXs += engine.snapshot().obstacleX
        }

        engine.update(elapsedSeconds = 0.05)

        assertEquals(1, eventObstacleXs.size)
        assertEquals(2, engine.snapshot().lives)
        assertTrue(engine.snapshot().isImpactFeedbackActive)

        repeat(4) {
            engine.update(elapsedSeconds = 0.05)
        }

        assertEquals(1, eventObstacleXs.size)
        assertEquals(2, engine.snapshot().lives)

        var previousObstacleX = engine.snapshot().obstacleX
        var framesUntilRespawn = 0
        var respawnObserved = false
        while (framesUntilRespawn < 100 && !respawnObserved) {
            if (framesUntilRespawn % 6 == 0) engine.flap()
            engine.update(elapsedSeconds = 0.05)
            framesUntilRespawn += 1

            val currentObstacleX = engine.snapshot().obstacleX
            assertEquals(1, eventObstacleXs.size)
            if (currentObstacleX > previousObstacleX) {
                assertTrue(
                    previousObstacleX - GameEngineConfig.OBSTACLE_SPEED * 0.05 <=
                        -GameEngineConfig.OBSTACLE_SIZE
                )
                assertTrue(currentObstacleX > 0.0)
                previousObstacleX = currentObstacleX
                respawnObserved = true
            } else {
                previousObstacleX = currentObstacleX
            }
        }
        assertTrue(respawnObserved)
        assertTrue(engine.snapshot().points > 0)
        var framesToSecondImpact = 0
        while (eventObstacleXs.size == 1 && framesToSecondImpact < 100) {
            engine.update(elapsedSeconds = 0.05)
            framesToSecondImpact += 1
        }

        assertEquals(2, eventObstacleXs.size)
        assertTrue(eventObstacleXs.last() > 0.0)
        assertEquals(1, engine.snapshot().lives)
        assertTrue(engine.snapshot().isImpactFeedbackActive)

        engine.restart()
        assertEquals(3, engine.snapshot().lives)
        assertFalse(engine.snapshot().isImpactFeedbackActive)
    }

    @Test
    fun impactFeedbackTimeFreezesWhilePaused() {
        val controller = GameController(gameAreaWidth = 240.0)
        controller.startGame()
        controller.update(elapsedSeconds = 0.05)
        assertTrue(controller.snapshot().isImpactFeedbackActive)

        controller.pauseGame()
        repeat(10) {
            controller.update(elapsedSeconds = 0.05)
        }
        assertTrue(controller.snapshot().isImpactFeedbackActive)

        controller.resumeGame()
        repeat(4) {
            controller.update(elapsedSeconds = 0.05)
        }
        assertFalse(controller.snapshot().isImpactFeedbackActive)

        controller.startGame()
        assertFalse(controller.snapshot().isImpactFeedbackActive)
    }

    @Test
    fun finalHitFlashClearsAfterLoss() {
        val engine = GameEngine(gameAreaWidth = 1280.0)

        repeat(3) { hit ->
            engine.handleDamageEvent(DamageEvent)
            if (hit < 2) {
                repeat(30) {
                    engine.update(elapsedSeconds = 0.05)
                }
            }
        }

        assertEquals(GameStatus.LOST, engine.snapshot().status)
        assertTrue(engine.snapshot().isImpactFeedbackActive)
        assertFalse(engine.snapshot().isInvulnerable)

        repeat(4) {
            engine.update(elapsedSeconds = 0.05)
        }
        assertFalse(engine.snapshot().isImpactFeedbackActive)

        engine.handleDamageEvent(DamageEvent)
        assertEquals(GameStatus.LOST, engine.snapshot().status)
        assertFalse(engine.snapshot().isImpactFeedbackActive)
    }

    @Test
    fun verticalCollisionEdgesAreStrictAtTopAndBottom() {
        val dragonX = GameEngineConfig.DRAGON_X
        val dragonWidth = GameEngineConfig.DRAGON_SIZE
        val obstacleX = dragonX
        val obstacleY = GameEngineConfig.OBSTACLE_Y
        val obstacleSize = GameEngineConfig.OBSTACLE_SIZE

        fun overlapsAt(dragonY: Double) = rectanglesOverlap(
            firstX = dragonX,
            firstY = dragonY,
            firstWidth = dragonWidth,
            firstHeight = dragonWidth,
            secondX = obstacleX,
            secondY = obstacleY,
            secondWidth = obstacleSize,
            secondHeight = obstacleSize
        )

        assertFalse(overlapsAt(obstacleY - dragonWidth))
        assertTrue(overlapsAt(obstacleY - dragonWidth + 0.01))
        assertTrue(overlapsAt(obstacleY + obstacleSize - 0.01))
        assertFalse(overlapsAt(obstacleY + obstacleSize))
    }

    @Test
    fun rectanglesTouchingAtEdgeDoNotOverlap() {
        assertFalse(
            rectanglesOverlap(
                firstX = 0.0,
                firstY = 0.0,
                firstWidth = 10.0,
                firstHeight = 10.0,
                secondX = 10.0,
                secondY = 0.0,
                secondWidth = 10.0,
                secondHeight = 10.0
            )
        )
    }
}
