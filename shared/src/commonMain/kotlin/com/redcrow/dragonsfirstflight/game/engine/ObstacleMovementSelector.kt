package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random

internal object ObstacleMovementSelector {
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
                GameZone.ZONE_ONE ->
                    listOf(ObstacleMovement.STATIC)

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
