package com.redcrow.dragonsfirstflight.ui.gameplay

internal const val FLYING_CROWS_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_crows.png"

internal const val FLYING_BATS_SPRITE_PATH: String =
    "sprites/obstacles/zone1/flying_bats.png"

internal val ANIMATED_OBSTACLE_CONFIGS = mapOf(
    FLYING_CROWS_SPRITE_PATH to AnimatedSpriteConfig(
        frameWidth = 384,
        frameHeight = 384,
        columns = 8,
        rows = 1,
        frameDurationMs = 120
    ),
    FLYING_BATS_SPRITE_PATH to AnimatedSpriteConfig(
        frameWidth = 418,
        frameHeight = 470,
        columns = 4,
        rows = 2,
        frameDurationMs = 120
    )
)
