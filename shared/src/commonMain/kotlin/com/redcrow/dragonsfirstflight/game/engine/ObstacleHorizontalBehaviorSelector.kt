package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

/** Randomly decides whether a new obstacle is a rush obstacle. */
internal object ObstacleHorizontalBehaviorSelector {
    private const val MEDIUM_RUSH_PROBABILITY = 0.15
    private const val HARD_RUSH_PROBABILITY = 0.30

    /** Never picks a rush on easy, in the boss zone, for bottom-only obstacles or right after another rush. */
    fun select(
        difficulty: GameDifficulty,
        zone: GameZone,
        isBottomOnly: Boolean,
        previousWasRush: Boolean,
        random: Random,
    ): ObstacleHorizontalBehavior {
        if (
            difficulty == GameDifficulty.EASY ||
            zone == GameZone.BOSS ||
            isBottomOnly ||
            previousWasRush
        ) {
            return ObstacleHorizontalBehavior.NORMAL
        }

        val rushProbability =
            when (difficulty) {
                GameDifficulty.EASY -> 0.0
                GameDifficulty.MEDIUM -> MEDIUM_RUSH_PROBABILITY
                GameDifficulty.HARD -> HARD_RUSH_PROBABILITY
            }

        if (random.nextDouble() >= rushProbability) {
            return ObstacleHorizontalBehavior.NORMAL
        }

        return when (difficulty) {
            GameDifficulty.EASY -> ObstacleHorizontalBehavior.NORMAL
            GameDifficulty.MEDIUM -> ObstacleHorizontalBehavior.RUSH_MEDIUM
            GameDifficulty.HARD -> ObstacleHorizontalBehavior.RUSH_HARD
        }
    }
}
