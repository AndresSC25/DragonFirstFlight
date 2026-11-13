package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.PI
import kotlin.math.sin

internal object ObstacleMovementCalculator {
    private const val BOB_AMPLITUDE: Double = 22.0
    private const val BOB_CYCLES_PER_SECOND: Double = 0.52
    private const val WAVE_AMPLITUDE: Double = 36.0
    private const val WAVE_CYCLES_PER_SECOND: Double = 0.72

    fun yAt(
        obstacle: Obstacle,
        activeTimeSeconds: Double,
    ): Double {
        require(activeTimeSeconds.isFinite() && activeTimeSeconds >= 0.0) {
            "Active time must be finite and must not be negative."
        }

        val offset =
            when (obstacle.movement) {
                ObstacleMovement.STATIC -> 0.0
                ObstacleMovement.BOB ->
                    verticalOffset(
                        activeTimeSeconds = activeTimeSeconds,
                        phase = obstacle.movementPhase,
                        amplitude = BOB_AMPLITUDE,
                        cyclesPerSecond = BOB_CYCLES_PER_SECOND,
                    )

                ObstacleMovement.WAVE ->
                    verticalOffset(
                        activeTimeSeconds = activeTimeSeconds,
                        phase = obstacle.movementPhase,
                        amplitude = WAVE_AMPLITUDE,
                        cyclesPerSecond = WAVE_CYCLES_PER_SECOND,
                    )
            }

        val minimumY = GameEngineConfig.DRAGON_MIN_Y
        val maximumY = GameEngineConfig.GAME_AREA_HEIGHT - obstacle.height

        return (obstacle.baseY + offset).coerceIn(minimumY, maximumY)
    }

    private fun verticalOffset(
        activeTimeSeconds: Double,
        phase: Double,
        amplitude: Double,
        cyclesPerSecond: Double,
    ): Double =
        sin(
            activeTimeSeconds * cyclesPerSecond * 2.0 * PI + phase,
        ) * amplitude
}
