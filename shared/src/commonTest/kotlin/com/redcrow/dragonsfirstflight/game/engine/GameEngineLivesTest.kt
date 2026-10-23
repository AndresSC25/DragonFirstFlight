package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.controller.GameController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineLivesTest {
    @Test
    fun damageDuringInvulnerabilityDoesNotSpendAnotherLife() {
        val receivedEvents = mutableListOf<DamageEvent>()
        val engine =
            GameEngine(gameAreaWidth = 1280.0) {
                receivedEvents += it
            }

        assertEquals(3, engine.snapshot().lives)
        assertFalse(engine.snapshot().isInvulnerable)

        engine.handleDamageEvent(DamageEvent)
        assertEquals(2, engine.snapshot().lives)
        assertTrue(engine.snapshot().isInvulnerable)

        engine.handleDamageEvent(DamageEvent)
        assertEquals(2, engine.snapshot().lives)
        assertEquals(2, receivedEvents.size)

        repeat(29) {
            engine.update(elapsedSeconds = 0.05)
        }
        assertTrue(engine.snapshot().isInvulnerable)
        engine.handleDamageEvent(DamageEvent)
        assertEquals(2, engine.snapshot().lives)

        engine.update(elapsedSeconds = 0.05)
        assertFalse(engine.snapshot().isInvulnerable)
        engine.handleDamageEvent(DamageEvent)
        assertEquals(1, engine.snapshot().lives)

        repeat(30) {
            engine.update(elapsedSeconds = 0.05)
        }
        engine.handleDamageEvent(DamageEvent)
        assertEquals(0, engine.snapshot().lives)
        assertEquals(GameStatus.LOST, engine.snapshot().status)
        assertFalse(engine.snapshot().isInvulnerable)

        engine.handleDamageEvent(DamageEvent)
        assertEquals(0, engine.snapshot().lives)
        assertEquals(5, receivedEvents.size)
    }

    @Test
    fun blinkAndCountdownPauseThenNewGameRestoresNormalState() {
        val controller = GameController(gameAreaWidth = 240.0)
        controller.startGame()
        controller.update(elapsedSeconds = 0.05)

        assertEquals(2, controller.snapshot().lives)
        assertTrue(controller.snapshot().isImpactFeedbackActive)
        assertTrue(controller.snapshot().isInvulnerable)
        assertTrue(controller.snapshot().isInvulnerabilityBlinkDim)

        repeat(5) {
            controller.update(elapsedSeconds = 0.05)
        }
        assertFalse(controller.snapshot().isImpactFeedbackActive)
        assertFalse(controller.snapshot().isInvulnerabilityBlinkDim)

        controller.pauseGame()
        repeat(40) {
            controller.update(elapsedSeconds = 0.05)
        }
        assertTrue(controller.snapshot().isInvulnerable)
        assertFalse(controller.snapshot().isInvulnerabilityBlinkDim)

        controller.resumeGame()
        repeat(4) {
            controller.update(elapsedSeconds = 0.05)
        }
        assertTrue(controller.snapshot().isInvulnerabilityBlinkDim)

        controller.returnToMenu()
        controller.startGame()
        assertEquals(3, controller.snapshot().lives)
        assertFalse(controller.snapshot().isInvulnerable)
        assertFalse(controller.snapshot().isInvulnerabilityBlinkDim)
        assertFalse(controller.snapshot().isImpactFeedbackActive)
    }
}
