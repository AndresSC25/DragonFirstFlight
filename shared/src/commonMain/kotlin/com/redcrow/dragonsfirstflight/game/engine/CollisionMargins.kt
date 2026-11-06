package com.redcrow.dragonsfirstflight.game.engine

internal data class CollisionMargins(
    val leftRatio: Double,
    val topRatio: Double,
    val rightRatio: Double,
    val bottomRatio: Double,
) {
    init {
        require(leftRatio.isValidRatio()) { "Left margin ratio must be between zero and one." }
        require(topRatio.isValidRatio()) { "Top margin ratio must be between zero and one." }
        require(rightRatio.isValidRatio()) { "Right margin ratio must be between zero and one." }
        require(bottomRatio.isValidRatio()) { "Bottom margin ratio must be between zero and one." }
        require(leftRatio + rightRatio < 1.0) {
            "Horizontal margins must leave a positive collision width."
        }
        require(topRatio + bottomRatio < 1.0) {
            "Vertical margins must leave a positive collision height."
        }
    }

    companion object {
        val NONE =
            CollisionMargins(
                leftRatio = 0.0,
                topRatio = 0.0,
                rightRatio = 0.0,
                bottomRatio = 0.0,
            )
    }
}

private fun Double.isValidRatio(): Boolean =
    isFinite() && this >= 0.0 && this < 1.0
