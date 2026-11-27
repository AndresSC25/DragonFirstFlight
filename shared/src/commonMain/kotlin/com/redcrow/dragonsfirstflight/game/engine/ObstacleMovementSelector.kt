package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

/** Randomly picks a vertical movement style, with more movement in later zones. */
internal object ObstacleMovementSelector {
    /** Bottom-only obstacles and the boss zone always get [ObstacleMovement.STATIC]. */
    fun select(
        zone: GameZone,
        isBottomOnly: Boolean,
        random: Random,
    ): ObstacleMovement {
        if (isBottomOnly) {
            return ObstacleMovement.STATIC
        }

        val candidates =
            when (zone) {
                GameZone.BOSS -> listOf(ObstacleMovement.STATIC)
                GameZone.ZONE_ONE ->
                    listOf(
                        ObstacleMovement.STATIC,
                        ObstacleMovement.STATIC,
                        ObstacleMovement.STATIC,
                        ObstacleMovement.BOB,
                    )

                GameZone.ZONE_TWO ->
                    listOf(
                        ObstacleMovement.STATIC,
                        ObstacleMovement.STATIC,
                        ObstacleMovement.BOB,
                    )

                GameZone.ZONE_THREE ->
                    listOf(
                        ObstacleMovement.STATIC,
                        ObstacleMovement.BOB,
                        ObstacleMovement.BOB,
                        ObstacleMovement.WAVE,
                    )
            }

        return candidates.random(random)
    }
}
