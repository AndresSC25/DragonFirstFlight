package com.redcrow.dragonsfirstflight.ui.gameplay

internal const val FLYING_CROWS_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_crows.png"

internal const val FLAMING_ARROW_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flaming_arrow.png"

internal const val KNIGHT_SWORD_SPRITE_PATH: String =
    "sprites/obstacles/zone1/knight_sword.png"

internal const val ZONE_TWO_FLYING_SKULL_WISP_SPRITE_PATH: String =
    "sprites/obstacles/zone2/flying _skull_wisp.png"

internal const val ZONE_TWO_FLYING_CURSED_BOOK_SPRITE_PATH: String =
    "sprites/obstacles/zone2/flying_cursed_book.png"

internal const val ZONE_TWO_FLYING_GARGOYLE_SPRITE_PATH: String =
    "sprites/obstacles/zone2/flying_gargoyle.png"

internal const val ZONE_TWO_GHOST_KNIGHT_SPRITE_PATH: String =
    "sprites/obstacles/zone2/ghost_knight.png"

internal const val ZONE_THREE_FAIRY_SPRITE_PATH: String =
    "sprites/obstacles/zone3/fairy_sprite.png"

internal const val ZONE_THREE_GIANT_HORNET_SPRITE_PATH: String =
    "sprites/obstacles/zone3/giant_hornet.png"

internal const val ZONE_THREE_WIND_SPIRIT_SPRITE_PATH: String =
    "sprites/obstacles/zone3/wind_spirit.png"

/** Sprite sheet layouts for animated obstacles, keyed by sprite path; paths missing here render as static images. */
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
        FLAMING_ARROW_SPRITE_PATH to
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
        ZONE_TWO_FLYING_CURSED_BOOK_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        ZONE_TWO_FLYING_GARGOYLE_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        ZONE_TWO_GHOST_KNIGHT_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        ZONE_THREE_FAIRY_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        ZONE_THREE_GIANT_HORNET_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
        ZONE_THREE_WIND_SPIRIT_SPRITE_PATH to
            AnimatedSpriteConfig(
                frameWidth = 384,
                frameHeight = 384,
                columns = 8,
                rows = 1,
                frameDurationMs = 120,
            ),
    )
