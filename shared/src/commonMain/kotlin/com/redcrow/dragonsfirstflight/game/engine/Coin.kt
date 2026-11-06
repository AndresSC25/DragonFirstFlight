package com.redcrow.dragonsfirstflight.game.engine

enum class CoinValue(
    val colones: Int,
) {
    HUNDRED(colones = 100),
    FIVE_HUNDRED(colones = 500),
}

/**
 * A collectible coin. [worldX] is fixed when the coin spawns; the screen position is
 * `worldX - GameSnapshot.coinScrollX`, so coins never need to be moved one by one.
 */
data class Coin(
    val worldX: Double,
    val y: Double,
    val value: CoinValue,
)
