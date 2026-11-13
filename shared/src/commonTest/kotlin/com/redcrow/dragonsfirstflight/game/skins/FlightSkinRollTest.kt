package com.redcrow.dragonsfirstflight.game.skins

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class FlightSkinRollTest {
    @Test
    fun teamSkinRollsExpectedRates() {
        val random = Random(42)
        val counts = mutableMapOf<String, Int>()
        repeat(200_000) {
            val path = rollFlightSpritePath(DragonSkin.SAPRISSA, random)
            counts[path] = counts.getOrElse(path) { 0 } + 1
        }

        val rainbow = counts.getOrElse(NyanSkin.RAINBOW.spritePath) { 0 }
        val team = counts.getOrElse(NyanSkin.SAPRISSA.spritePath) { 0 }
        val equipped = counts.getOrElse(DragonSkin.SAPRISSA.spritePath) { 0 }
        assertTrue(rainbow / 200_000.0 in 0.008..0.012)
        assertTrue(team / 200_000.0 in 0.094..0.104)
        assertTrue(rainbow + team + equipped == 200_000)
    }

    @Test
    fun brassRollsOnlyBrassOrRainbow() {
        val random = Random(42)
        var rainbowCount = 0
        repeat(50_000) {
            val path = rollFlightSpritePath(DragonSkin.BRASS, random)
            assertTrue(path == DragonSkin.BRASS.spritePath || path == NyanSkin.RAINBOW.spritePath)
            if (path == NyanSkin.RAINBOW.spritePath) rainbowCount++
        }
        assertTrue(rainbowCount > 0)
    }
}
