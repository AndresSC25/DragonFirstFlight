package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BossVolleyTest {
    @Test
    fun doubleReleasesSecondLockedTargetAfterDelayExactlyOnceAndReplaysFirePose() {
        val volley = BossVolley()
        val pattern = BossAttackPattern(BossAttackStage.DOUBLE, listOf(260.0, 540.0), isAimed = true)
        assertEquals(listOf(260.0), volley.start(pattern).map { it.centerY })
        assertNull(volley.secondaryFrameIndex)
        assertTrue(volley.update(GameEngineConfig.BOSS_DOUBLE_SHOT_DELAY_SECONDS - 0.01).isEmpty())
        val second = volley.update(0.01).single()
        assertEquals(540.0, second.centerY)
        assertEquals(pattern, second.pattern)
        assertEquals(5, volley.secondaryFrameIndex)
        assertTrue(volley.update(GameEngineConfig.CATAPULT_FIRE_SECONDS + 0.01).isEmpty())
        assertEquals(6, volley.secondaryFrameIndex)
        volley.update(GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS)
        assertEquals(7, volley.secondaryFrameIndex)
        assertTrue(volley.update(1.0).isEmpty())
        assertNull(volley.secondaryFrameIndex)
    }

    @Test
    fun corridorReleasesAllStonesTogetherAndSingleHasNoDelayedShot() {
        val volley = BossVolley()
        val corridor = BossPatternPlanner().next(44.0)
        assertEquals(corridor.projectileCenterYs.size, volley.start(corridor).size)
        assertTrue(volley.update(1.0).isEmpty())
        val single = BossAttackPattern(BossAttackStage.SINGLE, listOf(400.0))
        assertEquals(1, volley.start(single).size)
        assertTrue(volley.update(1.0).isEmpty())
    }

    @Test
    fun resetAndReplacementCancelUnreleasedSecondStone() {
        val volley = BossVolley()
        val double = BossAttackPattern(BossAttackStage.DOUBLE, listOf(260.0, 540.0))
        volley.start(double)
        volley.update(0.1)
        volley.reset()
        assertTrue(volley.update(1.0).isEmpty())
        assertNull(volley.secondaryFrameIndex)
        volley.start(double)
        volley.start(BossAttackPattern(BossAttackStage.SINGLE, listOf(400.0)))
        assertTrue(volley.update(1.0).isEmpty())
    }
}
