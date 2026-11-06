package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollisionBoundsCalculatorTest {
    @Test
    fun noMarginsPreserveOriginalBounds() {
        val bounds =
            CollisionBoundsCalculator.calculate(
                x = 10.0,
                y = 20.0,
                width = 80.0,
                height = 60.0,
                margins = CollisionMargins.NONE,
            )

        assertEquals(CollisionBounds(10.0, 20.0, 80.0, 60.0), bounds)
    }

    @Test
    fun normalizedMarginsScaleWithEntityDimensions() {
        val bounds =
            CollisionBoundsCalculator.calculate(
                x = 100.0,
                y = 200.0,
                width = 80.0,
                height = 40.0,
                margins =
                    CollisionMargins(
                        leftRatio = 0.10,
                        topRatio = 0.20,
                        rightRatio = 0.15,
                        bottomRatio = 0.10,
                    ),
            )

        assertEquals(108.0, bounds.x)
        assertEquals(208.0, bounds.y)
        assertEquals(60.0, bounds.width)
        assertEquals(28.0, bounds.height)
    }

    @Test
    fun movingEntityMovesItsCollisionBounds() {
        val first =
            CollisionBoundsCalculator.calculate(
                x = 400.0,
                y = 200.0,
                width = 72.0,
                height = 72.0,
                margins = CollisionMargins.NONE,
            )
        val second =
            CollisionBoundsCalculator.calculate(
                x = 400.0,
                y = 232.0,
                width = 72.0,
                height = 72.0,
                margins = CollisionMargins.NONE,
            )

        assertEquals(32.0, second.y - first.y)
    }

    @Test
    fun touchingBoundsDoNotOverlap() {
        val first = CollisionBounds(0.0, 0.0, 10.0, 10.0)
        val second = CollisionBounds(10.0, 0.0, 10.0, 10.0)

        assertFalse(collisionBoundsOverlap(first, second))
        assertTrue(
            collisionBoundsOverlap(
                first,
                second.copy(x = 9.99),
            ),
        )
    }

    @Test
    fun invalidMarginsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            CollisionMargins(
                leftRatio = 0.60,
                topRatio = 0.0,
                rightRatio = 0.40,
                bottomRatio = 0.0,
            )
        }
    }
}
