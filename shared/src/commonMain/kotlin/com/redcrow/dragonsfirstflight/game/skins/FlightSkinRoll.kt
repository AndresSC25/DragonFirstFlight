package com.redcrow.dragonsfirstflight.game.skins

import kotlin.random.Random

/** Rainbow appears about 1% of the time for every skin. */
internal const val RAINBOW_NYAN_CHANCE = 0.01

/** Team nyans appear about 9.9% overall because rainbow is rolled first. */
internal const val TEAM_NYAN_CHANCE = 0.10

internal fun rollFlightSpritePath(
    equipped: DragonSkin,
    random: Random,
): String {
    if (random.nextDouble() < RAINBOW_NYAN_CHANCE) return NyanSkin.RAINBOW.spritePath
    val nyan = equipped.nyan
    if (nyan != null && random.nextDouble() < TEAM_NYAN_CHANCE) return nyan.spritePath
    return equipped.spritePath
}
