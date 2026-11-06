package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ObstacleMovementCalculatorTest {
    @Test
    fun staticObstacleRemainsAtItsBasePosition() {
        val obstacle = createObstacle(ObstacleMovement.STATIC)

        repeat(20) { step ->
            assertEquals(
                obstacle.baseY,
                ObstacleMovementCalculator.yAt(
                    obstacle = obstacle,
                    activeTimeSeconds = step * 0.1,
                ),
            )
        }
    }

    @Test
    fun bobMovementChangesPositionWithinModerateRange() {
        val obstacle = createObstacle(ObstacleMovement.BOB)
        val positions =
            (0..40).map { step ->
                ObstacleMovementCalculator.yAt(
                    obstacle = obstacle,
                    activeTimeSeconds = step * 0.05,
                )
            }

        assertTrue(positions.any { it != obstacle.baseY })
        assertTrue(positions.all { it in 306.0..350.0 })
    }

    @Test
    fun waveMovementUsesPhaseAndRemainsInsideGameArea() {
        val obstacle =
            createObstacle(
                movement = ObstacleMovement.WAVE,
                baseY = GameEngineConfig.OBSTACLE_TOP_Y,
                phase = PI / 2.0,
            )
        val positions =
            (0..80).map { step ->
                ObstacleMovementCalculator.yAt(
                    obstacle = obstacle,
                    activeTimeSeconds = step * 0.05,
                )
            }

        assertTrue(positions.distinct().size > 1)
        assertTrue(
            positions.all { position ->
                position >= GameEngineConfig.DRAGON_MIN_Y &&
                    position + obstacle.height <=
                    GameEngineConfig.GAME_AREA_HEIGHT
            },
        )
    }

    @Test
    fun invalidActiveTimeIsRejected() {
        val obstacle = createObstacle(ObstacleMovement.BOB)

        assertFailsWith<IllegalArgumentException> {
            ObstacleMovementCalculator.yAt(
                obstacle = obstacle,
                activeTimeSeconds = -0.01,
            )
        }
    }

    private fun createObstacle(
        movement: ObstacleMovement,
        baseY: Double = GameEngineConfig.OBSTACLE_Y,
        phase: Double = 0.0,
    ): Obstacle =
        Obstacle(
            id = 1,
            x = 800.0,
            y = baseY,
            size = GameEngineConfig.OBSTACLE_SIZE,
            spritePath = "test/obstacle.png",
            baseY = baseY,
            movement = movement,
            movementPhase = phase,
        )
}
