package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

/** One obstacle slot of a pattern: its lane, source pattern and spacing and rush rules. */
internal data class ObstaclePatternStep(
    val lane: Double,
    val pattern: ObstaclePattern,
    /** True when this step must not become a rush obstacle. */
    val suppressRush: Boolean,
    /** Factor for the gap before this obstacle; above 1 only for the first step after an intense pattern. */
    val separationMultiplier: Double,
)

/** Hands out pattern lanes one obstacle at a time, picking a new pattern whenever the previous one is finished. */
internal class ObstaclePatternSequencer(
    private val random: Random,
) {
    private val pendingLanes = mutableListOf<Double>()
    private var activePattern: ObstaclePattern? = null
    private var lastPattern: ObstaclePattern? = null
    private var recoveryPending = false

    /** Avoids repeating the previous pattern when another exists; [singleLane] only applies to the single pattern. */
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

    /** Drops any partly consumed pattern and pattern history. */
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
