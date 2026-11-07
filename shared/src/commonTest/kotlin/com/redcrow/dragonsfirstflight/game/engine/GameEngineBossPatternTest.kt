package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineBossPatternTest {
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
