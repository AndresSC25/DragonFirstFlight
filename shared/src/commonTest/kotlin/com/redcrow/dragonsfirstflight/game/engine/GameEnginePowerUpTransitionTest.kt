package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.ceil
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEnginePowerUpTransitionTest {
    @Test
    fun firePowerAndFireBallsEndWhenZoneChanges() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = AlwaysSpawnRandom())
        val frames =
            flyUntil(engine, { it.isFirePowerActive }) { snapshot ->
                snapshot.firePowerUps.firstOrNull()?.y
            }
        engine.update(FRAME_SECONDS)
        assertTrue(engine.snapshot().isFirePowerActive)
        assertTrue(engine.snapshot().fireBalls.isNotEmpty())

        assertPowerUpsEndAtTransition(engine, GameZone.ZONE_TWO, frames + 1)
    }

    @Test
    fun shieldEndsWhenZoneChanges() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = AlwaysSpawnRandom())
        engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
        finishTransition(engine)
        val frames =
            flyUntil(engine, { it.isShieldActive }) { snapshot ->
                snapshot.shieldPowerUps.firstOrNull()?.y
            }
        assertTrue(engine.snapshot().isShieldActive)

        assertPowerUpsEndAtTransition(engine, GameZone.ZONE_THREE, frames)
    }

    @Test
    fun uncollectedFirePickupDisappearsWhenZoneChanges() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = AlwaysSpawnRandom())
        val frames = flyUntil(engine, { it.firePowerUps.isNotEmpty() })

        assertPowerUpsEndAtTransition(engine, GameZone.ZONE_TWO, frames)
    }

    @Test
    fun uncollectedShieldPickupDisappearsWhenZoneChanges() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = AlwaysSpawnRandom())
        engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
        finishTransition(engine)
        val frames = flyUntil(engine, { it.shieldPowerUps.isNotEmpty() })

        assertPowerUpsEndAtTransition(engine, GameZone.ZONE_THREE, frames)
    }

    private fun flyUntil(
        engine: GameEngine,
        condition: (GameSnapshot) -> Boolean,
        pickupY: (GameSnapshot) -> Double? = { null },
    ): Int {
        val zoneFrames = ceil(GameEngineConfig.ZONE_DURATION_SECONDS / FRAME_SECONDS).toInt()
        repeat(zoneFrames) { frame ->
            val snapshot = engine.snapshot()
            assertTrue(snapshot.isRunning)
            assertFalse(snapshot.isZoneTransitioning)
            if (condition(snapshot)) return frame
            val y = pickupY(snapshot)
            if (y != null && snapshot.dragonY >= y && snapshot.verticalVelocity >= 0.0) {
                engine.flap()
            }
            engine.update(FRAME_SECONDS)
        }
        error("Expected power-up state before the zone changed")
    }

    private fun assertPowerUpsEndAtTransition(
        engine: GameEngine,
        nextZone: GameZone,
        framesInZone: Int,
    ) {
        engine.update(GameEngineConfig.ZONE_DURATION_SECONDS - framesInZone * FRAME_SECONDS - FRAME_SECONDS / 2)
        assertFalse(engine.snapshot().isZoneTransitioning)
        engine.update(FRAME_SECONDS)
        val transition = engine.snapshot()
        assertTrue(transition.isZoneTransitioning)
        assertEquals(nextZone, transition.zone)
        assertPowerUpsCleared(transition)

        finishTransition(engine)
        assertPowerUpsCleared(engine.snapshot())
    }

    private fun finishTransition(engine: GameEngine) {
        val transitionFrames = ceil(GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS / FRAME_SECONDS).toInt()
        repeat(transitionFrames + 1) {
            if (engine.snapshot().isZoneTransitioning) {
                engine.update(FRAME_SECONDS)
            }
        }
        assertFalse(engine.snapshot().isZoneTransitioning)
    }

    private fun assertPowerUpsCleared(snapshot: GameSnapshot) {
        assertFalse(snapshot.isFirePowerActive, "Fire power carried into ${snapshot.zone}")
        assertFalse(snapshot.isShieldActive, "Shield carried into ${snapshot.zone}")
        assertTrue(snapshot.fireBalls.isEmpty(), "Fire balls carried into ${snapshot.zone}")
        assertTrue(snapshot.firePowerUps.isEmpty(), "Fire pickup carried into ${snapshot.zone}")
        assertTrue(snapshot.shieldPowerUps.isEmpty(), "Shield pickup carried into ${snapshot.zone}")
    }

    private class AlwaysSpawnRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }

    private companion object {
        const val FRAME_SECONDS = 0.05
    }
}
