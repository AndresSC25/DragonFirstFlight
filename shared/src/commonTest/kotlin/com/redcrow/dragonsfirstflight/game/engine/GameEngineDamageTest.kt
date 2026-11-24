package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineDamageTest {
    @Test
    fun overlappingObstacleEmitsDamageOnlyOnceAndRestartClearsRegistration() {
        val eventObstacleXs = mutableListOf<Double>()
        lateinit var engine: GameEngine
        engine =
            GameEngine(
                gameAreaWidth = 240.0,
                random = Random(7),
            ) {
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
                        -GameEngineConfig.OBSTACLE_SIZE,
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
        engine.restart()

        assertEquals(3, engine.snapshot().lives)
        assertFalse(engine.snapshot().isImpactFeedbackActive)
        assertEquals(1, eventObstacleXs.size)

        engine.update(elapsedSeconds = 0.05)

        assertEquals(2, eventObstacleXs.size)
        assertEquals(2, engine.snapshot().lives)
        assertTrue(engine.snapshot().isImpactFeedbackActive)

        repeat(4) {
            engine.update(elapsedSeconds = 0.05)
        }

        assertEquals(2, eventObstacleXs.size)
        assertEquals(2, engine.snapshot().lives)
    }

    @Test
    fun differentObstacleCanDamageDragonInSameRun() {
        val engine = GameEngine(gameAreaWidth = 480.0, random = Random(7))
        var snapshot = engine.snapshot()
        var firstImpactId: Int? = null
        var frames = 0

        while (snapshot.lives > 1 && frames < 150) {
            val nextObstacle = snapshot.obstacles.firstOrNull { it.id != firstImpactId }
            if (nextObstacle != null && snapshot.dragonY >= nextObstacle.y && snapshot.verticalVelocity >= 0.0) {
                engine.flap()
            }
            engine.update(elapsedSeconds = 0.05)
            snapshot = engine.snapshot()
            if (firstImpactId == null && snapshot.lives == 2) {
                firstImpactId = assertNotNull(snapshot.impactObstacleId)
            }
            frames += 1
        }

        assertNotNull(firstImpactId)
        assertEquals(1, snapshot.lives)
        assertNotEquals(firstImpactId, assertNotNull(snapshot.impactObstacleId))
        assertTrue(snapshot.isImpactFeedbackActive)
    }

    @Test
    fun hitEffectsEndWhenZoneChanges() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(7))
        val frameSeconds = 0.05

        engine.update(GameEngineConfig.ZONE_DURATION_SECONDS - 0.075)
        engine.handleDamageEvent(DamageEvent)
        val hit = engine.snapshot()
        assertTrue(hit.isInvulnerable)
        assertTrue(hit.isImpactFeedbackActive)

        repeat(2) {
            engine.update(frameSeconds)
        }
        val transition = engine.snapshot()
        assertTrue(transition.isZoneTransitioning)
        assertFalse(transition.isInvulnerable)
        assertFalse(transition.isImpactFeedbackActive)
        assertEquals(2, transition.lives)

        val transitionFrames = ceil(GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS / frameSeconds).toInt()
        repeat(transitionFrames + 1) {
            if (engine.snapshot().isZoneTransitioning) {
                engine.update(frameSeconds)
            }
        }
        assertFalse(engine.snapshot().isZoneTransitioning)
        assertFalse(engine.snapshot().isInvulnerable)
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

        fun overlapsAt(dragonY: Double) =
            rectanglesOverlap(
                firstX = dragonX,
                firstY = dragonY,
                firstWidth = dragonWidth,
                firstHeight = dragonWidth,
                secondX = obstacleX,
                secondY = obstacleY,
                secondWidth = obstacleSize,
                secondHeight = obstacleSize,
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
                secondHeight = 10.0,
            ),
        )
    }
}
