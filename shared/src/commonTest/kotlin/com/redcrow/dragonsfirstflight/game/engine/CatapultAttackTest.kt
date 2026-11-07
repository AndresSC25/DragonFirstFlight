package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CatapultAttackTest {
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
