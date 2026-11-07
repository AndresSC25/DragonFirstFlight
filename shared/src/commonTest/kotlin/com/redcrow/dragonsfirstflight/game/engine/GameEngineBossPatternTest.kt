package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineBossPatternTest {
    @Test
    fun aimedShotLocksDuringPreparationAndDoesNotFollowDragonAfterItMoves() {
        val engine = GameEngine(gameAreaWidth = 1280.0, random = Random(7))
        repeat(3) {
            engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
            repeat(110) {
                if (engine.snapshot().isZoneTransitioning) engine.update(0.05)
            }
        }
        var aimed: BossAttackPattern? = null
        repeat(220) {
            if (aimed == null) {
                engine.flap()
                engine.update(0.05)
                val catapult = assertNotNull(engine.snapshot().catapult)
                if (catapult.warningFrameIndex != null && catapult.attackPattern?.isAimed == true) {
                    aimed = catapult.attackPattern
                }
            }
        }
        val lockedPattern = assertNotNull(aimed)
        val lockedCenter = lockedPattern.projectileCenterYs.single()
        var shot: Projectile? = null
        repeat(60) {
            if (shot == null) {
                engine.update(0.05)
                assertEquals(lockedPattern, assertNotNull(engine.snapshot().catapult).attackPattern)
                shot = engine.snapshot().projectiles.firstOrNull()
            }
        }
        val fired = assertNotNull(shot)
        assertEquals(lockedCenter - fired.size / 2.0, fired.targetY)
        assertTrue(abs(engine.snapshot().dragonY + GameEngineConfig.DRAGON_SIZE / 2.0 - lockedCenter) > 80.0)
        repeat(20) { engine.update(0.05) }
        val inFlight = engine.snapshot().projectiles.single()
        assertEquals(fired.targetY, inFlight.targetY)
    }

    @Test
    fun eachStageReleasesExactlyTheWarnedFormationFromTheCatapult() {
        for ((stageIndex, expectedStage) in BossAttackStage.entries.withIndex()) {
            val engine = GameEngine(gameAreaWidth = 1280.0)
            repeat(3) {
                engine.update(GameEngineConfig.ZONE_DURATION_SECONDS)
                repeat(110) {
                    if (engine.snapshot().isZoneTransitioning) engine.update(0.05)
                }
            }
            if (stageIndex > 0) engine.update(stageIndex * GameEngineConfig.BOSS_STAGE_DURATION_SECONDS)
            var warned: BossAttackPattern? = null
            repeat(160) {
                if (engine.snapshot().projectiles.isEmpty()) {
                    engine.update(0.05)
                    val catapult = assertNotNull(engine.snapshot().catapult)
                    if (catapult.warningFrameIndex != null) warned = catapult.attackPattern
                }
            }
            val pattern = assertNotNull(warned)
            assertEquals(expectedStage, pattern.stage)
            val shots = engine.snapshot().projectiles
            assertEquals(pattern.projectileCenterYs.size, shots.size)
            assertEquals(pattern.projectileCenterYs.map { it - 32.0 }, shots.map { it.targetY })
            val catapult = assertNotNull(engine.snapshot().catapult)
            assertTrue(shots.all { it.y == catapult.y + catapult.size * 0.4 - 32.0 })
            assertTrue(shots.all { it.floatAmplitude == GameEngineConfig.BOSS_PROJECTILE_FLOAT_AMPLITUDE })
            engine.restart()
            assertTrue(engine.snapshot().projectiles.isEmpty())
            assertEquals(null, engine.snapshot().catapult)
        }
    }
}
