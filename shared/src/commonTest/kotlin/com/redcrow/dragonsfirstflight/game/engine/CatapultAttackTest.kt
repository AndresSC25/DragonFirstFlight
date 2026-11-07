package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatapultAttackTest {
    @Test
    fun furyShortensIdleAndLoadButKeepsFullWarningAndResetsToNormalTempo() {
        val attack = CatapultAttack()
        var shots = 0
        attack.update(1.0) { shots++ }
        attack.update(0.0, isFurious = true) { shots++ }
        assertEquals(0, attack.frameIndex)
        attack.update(GameEngineConfig.CATAPULT_FURY_ATTACK_INTERVAL_SECONDS / 2.0 - 0.01, isFurious = true) { shots++ }
        assertNull(attack.warningFrameIndex)
        attack.update(0.011, isFurious = true) { shots++ }
        assertEquals(1, attack.frameIndex)
        attack.update(GameEngineConfig.CATAPULT_FURY_LOAD_FRAME_SECONDS * 3, isFurious = true) { shots++ }
        assertEquals(4, attack.frameIndex)
        assertEquals(0, shots)
        attack.update(GameEngineConfig.CATAPULT_WARNING_SECONDS - 0.01, isFurious = true) { shots++ }
        assertEquals(0, shots)
        assertNotNull(attack.warningFrameIndex)
        attack.update(0.01, isFurious = true) { shots++ }
        assertEquals(1, shots)
        attack.reset()
        attack.update(1.3) { shots++ }
        assertEquals(0, attack.frameIndex)
        assertNull(attack.warningFrameIndex)
    }

    @Test
    fun furyCreatesMoreAttacksOverSameTimeWithoutDuplicateReleases() {
        val normal = CatapultAttack()
        val fury = CatapultAttack()
        var normalShots = 0
        var furyShots = 0
        repeat(200) {
            normal.update(0.05) { normalShots++ }
            fury.update(0.05, isFurious = true) { furyShots++ }
        }
        assertEquals(2, normalShots)
        assertEquals(3, furyShots)
        assertTrue(furyShots > normalShots)
    }

    @Test
    fun warnsAndLoadsBeforeReleasingExactlyOneProjectileThenRecovers() {
        val attack = CatapultAttack()
        var shots = 0

        fun advance(seconds: Double) = attack.update(seconds) { shots++ }

        assertEquals(0, attack.frameIndex)
        assertNull(attack.warningFrameIndex)
        advance(GameEngineConfig.CATAPULT_ATTACK_INTERVAL_SECONDS + 0.001)
        assertEquals(1, attack.frameIndex)
        assertNotNull(attack.warningFrameIndex)
        assertEquals(0, shots)

        advance(GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS)
        assertEquals(2, attack.frameIndex)
        advance(GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS)
        assertEquals(3, attack.frameIndex)
        advance(GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS)
        assertEquals(4, attack.frameIndex)
        assertNotNull(attack.warningFrameIndex)
        assertEquals(0, shots)

        advance(GameEngineConfig.CATAPULT_WARNING_SECONDS)
        assertEquals(5, attack.frameIndex)
        assertNull(attack.warningFrameIndex)
        assertEquals(1, shots)
        advance(0.01)
        assertEquals(1, shots)
        advance(GameEngineConfig.CATAPULT_FIRE_SECONDS)
        assertEquals(6, attack.frameIndex)
        advance(GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS)
        assertEquals(7, attack.frameIndex)
        advance(GameEngineConfig.CATAPULT_RECOVERY_FRAME_SECONDS)
        assertEquals(0, attack.frameIndex)
        assertEquals(1, shots)

        advance(
            GameEngineConfig.CATAPULT_ATTACK_INTERVAL_SECONDS +
                GameEngineConfig.CATAPULT_LOAD_FRAME_SECONDS * 3 +
                GameEngineConfig.CATAPULT_WARNING_SECONDS,
        )
        assertEquals(2, shots)
    }

    @Test
    fun resetCancelsPendingAttackAndRestartsDelay() {
        val attack = CatapultAttack()
        var shots = 0
        attack.update(GameEngineConfig.CATAPULT_ATTACK_INTERVAL_SECONDS + 0.1) { shots++ }
        assertNotNull(attack.warningFrameIndex)
        attack.reset()
        assertEquals(0, attack.frameIndex)
        assertNull(attack.warningFrameIndex)
        attack.update(GameEngineConfig.CATAPULT_WARNING_SECONDS) { shots++ }
        assertEquals(0, shots)
        assertEquals(0, attack.frameIndex)
    }
}
