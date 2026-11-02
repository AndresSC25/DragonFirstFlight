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

internal const val ZONE_TWO_BROKEN_TOWER_ARCH_SPRITE_PATH: String =
    "sprites/obstacles/zone2/broken_tower_arch.png"

internal const val ZONE_TWO_FIRE_BRAZIER_SPRITE_PATH: String =
    "sprites/obstacles/zone2/fire_brazier.png"

internal const val ZONE_TWO_WALL_SPIKES_SPRITE_PATH: String =
    "sprites/obstacles/zone2/wall_spikes.png"

internal const val ZONE_TWO_WOODEN_BEAM_SPRITE_PATH: String =
    "sprites/obstacles/zone2/wooden_beam.png"

internal const val ZONE_THREE_FALLEN_LOG_SPRITE_PATH: String =
    "sprites/obstacles/zone3/fallen_log.png"

internal const val ZONE_THREE_HANGING_VINES_SPRITE_PATH: String =
    "sprites/obstacles/zone3/hanging_vines.png"

internal const val ZONE_THREE_ROCK_CLUSTER_SPRITE_PATH: String =
    "sprites/obstacles/zone3/rock_cluster.png"

internal const val ZONE_THREE_THORN_BUSH_SPRITE_PATH: String =
    "sprites/obstacles/zone3/thorn_bush.png"

internal const val ZONE_THREE_TREE_BRANCH_SPRITE_PATH: String =
    "sprites/obstacles/zone3/tree_branch.png"

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
