package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal data class ObstaclePatternStep(
    val lane: Double,
    val pattern: ObstaclePattern,
    val suppressRush: Boolean,
    val separationMultiplier: Double,
)

internal class ObstaclePatternSequencer(
    private val random: Random,
) {
    private val pendingLanes = mutableListOf<Double>()
    private var activePattern: ObstaclePattern? = null
    private var lastPattern: ObstaclePattern? = null
    private var recoveryPending = false

    fun nextStep(
        zone: GameZone,
        difficulty: GameDifficulty,
        singleLane: Double,
    ): ObstaclePatternStep {
        var separationMultiplier = 1.0

        if (pendingLanes.isEmpty()) {
            if (recoveryPending) {
                separationMultiplier = recoveryMultiplier(difficulty)
                recoveryPending = false
            }

            val patterns =
                ObstaclePatternPlanner.availablePatterns(
                    zone = zone,
                    difficulty = difficulty,
                )
            require(patterns.isNotEmpty()) {
                "At least one obstacle pattern must be available."
            }

            val candidates =
                patterns.filter { pattern ->
                    pattern != lastPattern || patterns.distinct().size == 1
                }
            val selectedPattern = candidates.random(random)
            val reverseVariant = random.nextBoolean()

            pendingLanes +=
                ObstaclePatternPlanner.lanesFor(
                    pattern = selectedPattern,
                    singleLane = singleLane,
                    reverseVariant = reverseVariant,
                )
            activePattern = selectedPattern
            lastPattern = selectedPattern
        }

        val pattern = checkNotNull(activePattern)
        val lane = pendingLanes.removeAt(0)
        val suppressRush =
            pattern == ObstaclePattern.ZIGZAG ||
                pattern == ObstaclePattern.ALTERNATING ||
                pattern == ObstaclePattern.DOUBLE_PRESSURE

        if (pendingLanes.isEmpty()) {
            recoveryPending = pattern.isIntense
            activePattern = null
        }

        return ObstaclePatternStep(
            lane = lane,
            pattern = pattern,
            suppressRush = suppressRush,
            separationMultiplier = separationMultiplier,
        )
    }

    fun reset() {
        pendingLanes.clear()
        activePattern = null
        lastPattern = null
        recoveryPending = false
    }

    private fun recoveryMultiplier(difficulty: GameDifficulty): Double =
        when (difficulty) {
            GameDifficulty.EASY -> 1.20
            GameDifficulty.MEDIUM -> 1.30
            GameDifficulty.HARD -> 1.40
        }
}
