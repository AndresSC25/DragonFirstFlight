package com.redcrow.dragonsfirstflight.game.engine

internal fun rectanglesOverlap(
    firstX: Double,
    firstY: Double,
    firstWidth: Double,
    firstHeight: Double,
    secondX: Double,
    secondY: Double,
    secondWidth: Double,
    secondHeight: Double
): Boolean {
    require(firstWidth >= 0.0) {
        "firstWidth must not be negative."
    }

    require(firstHeight >= 0.0) {
        "firstHeight must not be negative."
    }

    require(secondWidth >= 0.0) {
        "secondWidth must not be negative."
    }

    require(secondHeight >= 0.0) {
        "secondHeight must not be negative."
    }

    return firstX < secondX + secondWidth &&
            firstX + firstWidth > secondX &&
            firstY < secondY + secondHeight &&
            firstY + firstHeight > secondY
}

