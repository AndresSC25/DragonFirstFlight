package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RushWarningTimingTest {
    @Test
    fun rushObstacleStartsWithFullWarningDuration() {
        val rush = findNewRushObstacle()

        assertEquals(
            GameEngineConfig.RUSH_WARNING_DURATION_SECONDS,
            rush.obstacle.rushWarningRemainingSeconds,
            absoluteTolerance = TOLERANCE,
        )
        assertTrue(rush.obstacle.horizontalBehavior.isRush)
    }

    @Test
    fun rushObstacleRemainsStationaryDuringWarning() {
        val rush = findNewRushObstacle()
        val initialX = rush.obstacle.x

        rush.generator.update(
            delta = PARTIAL_WARNING_SECONDS,
            activeTimeSeconds =
                rush.activeTimeSeconds + PARTIAL_WARNING_SECONDS,
        )

        val updated = obstacleById(rush.generator, rush.obstacle.id)

        assertEquals(initialX, updated.x, absoluteTolerance = TOLERANCE)
        assertEquals(
            GameEngineConfig.RUSH_WARNING_DURATION_SECONDS -
                PARTIAL_WARNING_SECONDS,
            updated.rushWarningRemainingSeconds,
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun rushObstacleUsesOnlyFrameTimeRemainingAfterWarning() {
        val rush = findNewRushObstacle()
        val initialX = rush.obstacle.x
        val updateDelta =
            GameEngineConfig.RUSH_WARNING_DURATION_SECONDS +
                POST_WARNING_MOVEMENT_SECONDS

        rush.generator.update(
            delta = updateDelta,
            activeTimeSeconds = rush.activeTimeSeconds + updateDelta,
        )

        val updated = obstacleById(rush.generator, rush.obstacle.id)
        val expectedDistance =
            GameDifficulty.HARD
                .obstacleSpeedAt(rush.activeTimeSeconds + updateDelta) *
                rush.obstacle.horizontalBehavior.speedMultiplier *
                POST_WARNING_MOVEMENT_SECONDS

        assertEquals(
            expectedDistance,
            initialX - updated.x,
            absoluteTolerance = TOLERANCE,
        )
        assertEquals(
            0.0,
            updated.rushWarningRemainingSeconds,
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun normalObstacleMovesWithoutWarningDelay() {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
                difficulty = GameDifficulty.EASY,
            )
        val before = assertNotNull(generator.snapshot().firstOrNull())

        generator.update(
            delta = FRAME_DELTA,
            activeTimeSeconds = FRAME_DELTA,
        )

        val after = obstacleById(generator, before.id)
        val expectedDistance =
            GameDifficulty.EASY.obstacleSpeedAt(FRAME_DELTA) *
                FRAME_DELTA

        assertEquals(
            expectedDistance,
            before.x - after.x,
            absoluteTolerance = TOLERANCE,
        )
        assertEquals(0.0, after.rushWarningRemainingSeconds)
    }

    private fun findNewRushObstacle(): RushFixture {
        val generator =
            ObstacleGenerator(
                areaWidth = 1280.0,
                random = Random(7),
                difficulty = GameDifficulty.HARD,
            )

        repeat(MAX_FRAMES) { frame ->
            val activeTimeSeconds = frame * FRAME_DELTA
            generator.update(
                delta = FRAME_DELTA,
                activeTimeSeconds = activeTimeSeconds,
            )

            val rush =
                generator.snapshot().firstOrNull { obstacle ->
                    obstacle.horizontalBehavior.isRush &&
                        obstacle.rushWarningRemainingSeconds ==
                            GameEngineConfig.RUSH_WARNING_DURATION_SECONDS
                }

            if (rush != null) {
                return RushFixture(
                    generator = generator,
                    obstacle = rush,
                    activeTimeSeconds = activeTimeSeconds,
                )
            }
        }

        error("A newly generated rush obstacle was not found.")
    }

    private fun obstacleById(
        generator: ObstacleGenerator,
        obstacleId: Int,
    ): Obstacle =
        assertNotNull(
            generator.snapshot().firstOrNull { obstacle ->
                obstacle.id == obstacleId
            },
        )

    private data class RushFixture(
        val generator: ObstacleGenerator,
        val obstacle: Obstacle,
        val activeTimeSeconds: Double,
    )

    private companion object {
        const val FRAME_DELTA = 0.05
        const val PARTIAL_WARNING_SECONDS = 0.20
        const val POST_WARNING_MOVEMENT_SECONDS = 0.05
        const val MAX_FRAMES = 1200
        const val TOLERANCE = 0.000001
    }
}
