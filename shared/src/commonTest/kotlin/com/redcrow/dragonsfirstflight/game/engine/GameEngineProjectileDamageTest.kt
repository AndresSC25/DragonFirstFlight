package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineProjectileDamageTest {
    private fun enterBoss(engine: GameEngine) {
        repeat(3) {
            engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            repeat(110) {
                if (engine.snapshot().isZoneTransitioning) engine.update(0.05)
            }
        }
    }

    private fun followFiringHeight(engine: GameEngine) {
        // Follow the nearest warned lane to exercise damage with real patterned boss shots.
        val snapshot = engine.snapshot()
        val catapult = assertNotNull(snapshot.catapult)
        val dragonCenter = snapshot.dragonY + GameEngineConfig.DRAGON_SIZE / 2.0
        val targetCenter =
            catapult.attackPattern?.projectileCenterYs?.minByOrNull { abs(it - dragonCenter) }
                ?: (catapult.y + catapult.size * GameEngineConfig.CATAPULT_RELEASE_Y_FRACTION)
        val targetY =
            targetCenter - GameEngineConfig.DRAGON_SIZE / 2.0
        if (snapshot.dragonY > targetY) engine.flap()
        engine.update(0.05)
    }

    @Test
    fun projectileHitCostsOneLifeRemovesRockAndKeepsImpactAfterRemoval() {
        var damageEvents = 0
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(42), onDamageEvent = { damageEvents++ })
        enterBoss(engine)
        var touchingShotId: Int? = null
        repeat(300) {
            if (damageEvents > 0) return@repeat
            val before = engine.snapshot()
            touchingShotId = before.projectiles.firstOrNull()?.id ?: touchingShotId
            followFiringHeight(engine)
        }
        val hit = engine.snapshot()
        assertEquals(1, damageEvents)
        assertEquals(GameEngineConfig.INITIAL_LIVES - 1, hit.lives)
        assertTrue(hit.isImpactFeedbackActive)
        assertTrue(hit.isInvulnerable)
        assertNull(hit.impactObstacleId)
        assertNotNull(hit.impactPosition)
        assertTrue(hit.projectiles.none { it.id == touchingShotId })

        engine.update(0.05)
        assertEquals(hit.lives, engine.snapshot().lives)
        assertEquals(hit.impactPosition, engine.snapshot().impactPosition)
        assertEquals(1, damageEvents)
        repeat(4) { engine.update(0.05) }
        assertFalse(engine.snapshot().isImpactFeedbackActive)
        assertNull(engine.snapshot().impactPosition)

        engine.restart()
        assertEquals(GameEngineConfig.INITIAL_LIVES, engine.snapshot().lives)
        assertFalse(engine.snapshot().isInvulnerable)
        assertNull(engine.snapshot().impactPosition)
        assertTrue(engine.snapshot().projectiles.isEmpty())
    }

    @Test
    fun invulnerabilityConsumesIncomingRockWithoutTakingAnotherLife() {
        var damageEvents = 0
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(42), onDamageEvent = { damageEvents++ })
        enterBoss(engine)
        var incoming: Projectile? = null
        repeat(300) {
            if (incoming != null) return@repeat
            followFiringHeight(engine)
            incoming = engine.snapshot().projectiles.firstOrNull { it.x in 230.0..250.0 }
        }
        val rock = assertNotNull(incoming)
        assertEquals(0, damageEvents)
        engine.handleDamageEvent(DamageEvent)
        val protectedLives = engine.snapshot().lives
        repeat(12) { followFiringHeight(engine) }
        assertEquals(2, damageEvents)
        assertEquals(protectedLives, engine.snapshot().lives)
        assertTrue(engine.snapshot().isInvulnerable)
        assertTrue(engine.snapshot().projectiles.none { it.id == rock.id })
    }

    @Test
    fun threeSeparatedProjectileHitsEndRunAndStopAttacks() {
        var damageEvents = 0
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(42), onDamageEvent = { damageEvents++ })
        enterBoss(engine)
        repeat(550) {
            if (engine.isRunning) followFiringHeight(engine)
        }
        val defeated = engine.snapshot()
        assertEquals(GameStatus.LOST, defeated.status)
        assertEquals(0, defeated.lives)
        assertEquals(GameEngineConfig.INITIAL_LIVES, damageEvents)
        assertTrue(defeated.projectiles.isEmpty())
        assertNull(assertNotNull(defeated.catapult).warningFrameIndex)
        repeat(10) { engine.update(0.05) }
        assertEquals(0, engine.snapshot().lives)
        assertEquals(damageEvents, GameEngineConfig.INITIAL_LIVES)
        assertTrue(engine.snapshot().projectiles.isEmpty())
        assertFalse(engine.snapshot().isImpactFeedbackActive)
    }
}
