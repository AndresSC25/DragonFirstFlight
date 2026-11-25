package com.redcrow.dragonsfirstflight.game.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class GameFeedbackCountersTest {
    @Test
    fun damageCounterIgnoresInvulnerableHitsAndResetsOnRetry() {
        val engine = GameEngine(1280.0)
        engine.handleDamageEvent(DamageEvent)
        engine.handleDamageEvent(DamageEvent)
        assertEquals(1, engine.snapshot().damageCount)
        assertEquals(2, engine.snapshot().lives)
        engine.restart()
        assertEquals(0, engine.snapshot().damageCount)
        assertEquals(0, engine.snapshot().extraLivesCollected)
        engine.handleDamageEvent(DamageEvent)
        assertEquals(1, engine.snapshot().damageCount)
    }
}
