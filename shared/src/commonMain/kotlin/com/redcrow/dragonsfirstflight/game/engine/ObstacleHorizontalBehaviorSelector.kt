package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal object ObstacleHorizontalBehaviorSelector {
    private const val MEDIUM_RUSH_PROBABILITY = 0.15
    private const val HARD_RUSH_PROBABILITY = 0.30

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
