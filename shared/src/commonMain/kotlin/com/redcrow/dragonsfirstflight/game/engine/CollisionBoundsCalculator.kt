package com.redcrow.dragonsfirstflight.game.engine

/** Turns a sprite's frame rectangle into its hit rectangle by cutting off per-side margin ratios. */
internal object CollisionBoundsCalculator {
    /** Margin ratios apply to the frame [width] and [height]; throws on non-finite input or a non-positive size. */
    fun calculate(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        margins: CollisionMargins,
    ): CollisionBounds {
        require(x.isFinite()) { "Entity x must be finite." }
        require(y.isFinite()) { "Entity y must be finite." }
        require(width.isFinite() && width > 0.0) {
            "Entity width must be finite and greater than zero."
        }
        require(height.isFinite() && height > 0.0) {
            "Entity height must be finite and greater than zero."
        }

        val leftMargin = width * margins.leftRatio
        val topMargin = height * margins.topRatio
        val rightMargin = width * margins.rightRatio
        val bottomMargin = height * margins.bottomRatio

        return CollisionBounds(
            x = x + leftMargin,
            y = y + topMargin,
            width = width - leftMargin - rightMargin,
            height = height - topMargin - bottomMargin,
        )
    }
}
