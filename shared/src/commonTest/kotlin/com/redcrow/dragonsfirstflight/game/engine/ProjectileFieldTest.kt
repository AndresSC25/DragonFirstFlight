package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectileFieldTest {
    @Test
    fun movesByVelocityAndPreservesPreviouslyPublishedSnapshot() {
        val field = ProjectileField()
        field.launch(x = 200.0, y = 300.0, size = 64.0, velocityX = -400.0)
        val original = field.snapshot().single()
        field.update(delta = 0.25, leftBoundary = 0.0, rightBoundary = 1280.0)
        val moved = field.snapshot().single()
        assertEquals(original.id, moved.id)
        assertEquals(100.0, moved.x)
        assertEquals(original.y, moved.y)
        assertEquals(0.25, moved.ageSeconds)
        assertEquals(200.0, original.x)
        assertEquals(0.0, original.ageSeconds)
    }

    @Test
    fun removesProjectileOnlyAfterItFullyLeavesActualViewport() {
        val field = ProjectileField()
        field.launch(x = 0.0, y = 300.0, size = 64.0, velocityX = -400.0)
        field.update(delta = 0.5, leftBoundary = -160.0, rightBoundary = 1440.0)
        assertEquals(-200.0, field.snapshot().single().x)
        field.update(delta = 0.1, leftBoundary = -160.0, rightBoundary = 1440.0)
        assertTrue(field.snapshot().isEmpty())
    }

    @Test
    fun transparentPaddingAndTouchingEdgesDoNotHitButVisibleRockIsConsumedOnce() {
        val field = ProjectileField()
        field.launch(x = 200.0, y = 300.0, size = 64.0, velocityX = 0.0)
        assertTrue(field.removeColliding(CollisionBounds(200.0, 300.0, 10.0, 10.0)).isEmpty())
        assertTrue(field.removeColliding(CollisionBounds(250.5, 320.0, 10.0, 10.0)).isEmpty())
        assertEquals(1, field.snapshot().size)

        val hits = field.removeColliding(CollisionBounds(220.0, 320.0, 10.0, 10.0))
        assertEquals(1, hits.size)
        assertTrue(field.snapshot().isEmpty())
        assertTrue(field.removeColliding(CollisionBounds(220.0, 320.0, 10.0, 10.0)).isEmpty())
    }

    @Test
    fun simultaneousHitsAreRemovedTogetherWithoutRemovingMisses() {
        val field = ProjectileField()
        field.launch(x = 200.0, y = 300.0, size = 64.0, velocityX = 0.0)
        field.launch(x = 210.0, y = 300.0, size = 64.0, velocityX = 0.0)
        field.launch(x = 600.0, y = 300.0, size = 64.0, velocityX = 0.0)
        assertEquals(2, field.removeColliding(CollisionBounds(220.0, 320.0, 40.0, 40.0)).size)
        assertEquals(600.0, field.snapshot().single().x)
    }

    @Test
    fun ballisticStoneMovesUnderGravityAndIsRemovedWhenItFallsOffScreen() {
        val field = ProjectileField()
        field.launch(x = 900.0, y = 400.0, size = 64.0, velocityX = -100.0, velocityY = -300.0, gravity = 300.0)
        field.update(delta = 1.0, leftBoundary = 0.0, rightBoundary = 1280.0)
        val apex = field.snapshot().single()
        assertEquals(800.0, apex.x)
        assertEquals(250.0, apex.y)
        field.update(delta = 1.0, leftBoundary = 0.0, rightBoundary = 1280.0)
        assertEquals(400.0, field.snapshot().single().y)
        field.update(delta = 1.0, leftBoundary = 0.0, rightBoundary = 1280.0)
        assertTrue(field.snapshot().isEmpty())
    }

    @Test
    fun resetClearsActiveProjectiles() {
        val field = ProjectileField()
        field.launch(x = 600.0, y = 300.0, size = 64.0, velocityX = -400.0)
        field.reset()
        assertTrue(field.snapshot().isEmpty())
    }
}
