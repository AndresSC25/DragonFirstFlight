package com.redcrow.dragonsfirstflight.game.engine

internal object CollisionProfileCatalog {
    // Visible envelope shared by the eight 128x128 rock frames (pixels 27 through 100).
    val bossProjectileMargins =
        CollisionMargins(
            leftRatio = 27.0 / 128.0,
            topRatio = 27.0 / 128.0,
            rightRatio = 27.0 / 128.0,
            bottomRatio = 27.0 / 128.0,
        )

    val dragonMargins =
        CollisionMargins(
            leftRatio = 2.0 / 32.0,
            topRatio = 4.0 / 32.0,
            rightRatio = 1.0 / 32.0,
            bottomRatio = 1.0 / 32.0,
        )

    private val obstacleMarginsBySpritePath =
        mapOf(
            "sprites/obstacles/zone1/flaming_arrow.png" to margins(39, 22, 26, 37),
            "sprites/obstacles/zone1/flying_bat.png" to margins(33, 20, 9, 10),
            "sprites/obstacles/zone1/flying_crows.png" to margins(42, 44, 12, 56),
            "sprites/obstacles/zone1/flying_sword.png" to margins(26, 34, 21, 44),
            "sprites/obstacles/zone1/forest_mage.png" to margins(76, 42, 75, 21),
            "sprites/obstacles/zone1/knight_sword.png" to margins(8, 41, 15, 23),
            "sprites/obstacles/zone2/flying _skull_wisp.png" to margins(38, 3, 28, 31),
            "sprites/obstacles/zone2/flying_cursed_book.png" to margins(28, 24, 33, 29),
            "sprites/obstacles/zone2/flying_gargoyle.png" to margins(32, 25, 38, 39),
            "sprites/obstacles/zone2/ghost_knight.png" to margins(59, 13, 105, 17),
            "sprites/obstacles/zone2/winged_imp.png" to margins(26, 21, 33, 41),
            "sprites/obstacles/zone3/fairy_sprite.png" to margins(39, 13, 43, 26),
            "sprites/obstacles/zone3/flying_forest_monster.png" to margins(37, 1, 18, 17),
            "sprites/obstacles/zone3/giant_hornet.png" to margins(60, 15, 41, 43),
            "sprites/obstacles/zone3/wind_spirit.png" to margins(95, 12, 95, 27),
        )

    fun obstacleMargins(spritePath: String): CollisionMargins =
        obstacleMarginsBySpritePath[spritePath]
            ?: error("Missing collision profile for obstacle: $spritePath")

    internal fun configuredObstaclePaths(): Set<String> = obstacleMarginsBySpritePath.keys

    private fun margins(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): CollisionMargins =
        CollisionMargins(
            leftRatio = left / OBSTACLE_FRAME_SIZE,
            topRatio = top / OBSTACLE_FRAME_SIZE,
            rightRatio = right / OBSTACLE_FRAME_SIZE,
            bottomRatio = bottom / OBSTACLE_FRAME_SIZE,
        )

    private const val OBSTACLE_FRAME_SIZE: Double = 384.0
}
