package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal class ObstaclePatternSequencer(
    private val random: Random,
) {
    private val pendingLanes = mutableListOf<Double>()
    private var lastPattern: ObstaclePattern? = null

    fun nextLane(
        zone: GameZone,
        singleLane: Double,
    ): Double {
        if (pendingLanes.isEmpty()) {
            val patterns = ObstaclePatternPlanner.availablePatterns(zone)
            val candidates =
                patterns.filter { pattern ->
                    pattern != lastPattern || patterns.size == 1
                }
            val selectedPattern = candidates.random(random)
            pendingLanes +=
                ObstaclePatternPlanner.lanesFor(
                    pattern = selectedPattern,
                    singleLane = singleLane,
                )
            lastPattern = selectedPattern
        }

        return pendingLanes.removeAt(0)
    }

    fun reset() {
        pendingLanes.clear()
        lastPattern = null
    }
}

