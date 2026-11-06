package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CollisionProfileCatalogTest {
    private val generatedObstaclePaths =
        setOf(
            "sprites/obstacles/zone1/flying_bat.png",
            "sprites/obstacles/zone1/flying_crows.png",
            "sprites/obstacles/zone1/flying_sword.png",
            "sprites/obstacles/zone1/flaming_arrow.png",
            "sprites/obstacles/zone1/forest_mage.png",
            "sprites/obstacles/zone1/knight_sword.png",
            "sprites/obstacles/zone2/flying _skull_wisp.png",
            "sprites/obstacles/zone2/flying_cursed_book.png",
            "sprites/obstacles/zone2/flying_gargoyle.png",
            "sprites/obstacles/zone2/ghost_knight.png",
            "sprites/obstacles/zone2/winged_imp.png",
            "sprites/obstacles/zone3/fairy_sprite.png",
            "sprites/obstacles/zone3/flying_forest_monster.png",
            "sprites/obstacles/zone3/giant_hornet.png",
            "sprites/obstacles/zone3/wind_spirit.png",
        )

    @Test
    fun everyGeneratedObstacleHasACollisionProfile() {
        generatedObstaclePaths.forEach { spritePath ->
            val margins =
                CollisionProfileCatalog.obstacleMargins(spritePath)

            assertTrue(margins.leftRatio >= 0.0)
            assertTrue(margins.topRatio >= 0.0)
            assertTrue(margins.rightRatio >= 0.0)
            assertTrue(margins.bottomRatio >= 0.0)
        }
    }

    @Test
    fun dragonMarginsMatchMeasuredAlphaBounds() {
        val margins = CollisionProfileCatalog.dragonMargins

        assertEquals(2.0 / 32.0, margins.leftRatio)
        assertEquals(4.0 / 32.0, margins.topRatio)
        assertEquals(1.0 / 32.0, margins.rightRatio)
        assertEquals(1.0 / 32.0, margins.bottomRatio)
    }

    @Test
    fun windSpiritUsesItsLargeHorizontalMargins() {
        val margins =
            CollisionProfileCatalog.obstacleMargins(
                "sprites/obstacles/zone3/wind_spirit.png",
            )

        assertEquals(95.0 / 384.0, margins.leftRatio)
        assertEquals(95.0 / 384.0, margins.rightRatio)
    }

    @Test
    fun unknownObstacleIsRejected() {
        assertFailsWith<IllegalStateException> {
            CollisionProfileCatalog.obstacleMargins(
                "sprites/obstacles/unknown.png",
            )
        }
    }
}
