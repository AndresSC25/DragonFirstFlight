package com.redcrow.dragonsfirstflight.ui.gameplay

internal const val FLYING_CROWS_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_crows.png"

internal const val FLYING_BAT_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_bat.png"

internal const val FOREST_MAGE_SPRITE_PATH: String =
    "sprites/obstacles/zone1/forest_mage.png"

internal const val FLAMING_ARROW_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flaming_arrow.png"

internal const val KNIGHT_SWORD_SPRITE_PATH: String =
    "sprites/obstacles/zone1/knight_sword.png"

internal const val FLYING_SWORD_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_sword.png"

internal val ANIMATED_OBSTACLE_CONFIGS =
    mapOf(
        FLYING_CROWS_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        FLYING_BAT_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        FLYING_SWORD_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        FLAMING_ARROW_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        FOREST_MAGE_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        KNIGHT_SWORD_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
    )
