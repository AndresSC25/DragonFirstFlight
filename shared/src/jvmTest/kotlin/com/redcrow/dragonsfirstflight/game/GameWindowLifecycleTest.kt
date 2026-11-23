package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.controller.GameController
import korlibs.event.FocusEvent
import korlibs.event.ResumeEvent
import korlibs.render.GameWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameWindowLifecycleTest {
    @Test
    fun losingFocusFreezesTheRunUntilThePlayerResumes() {
        val controller = GameController(gameAreaWidth = 1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        controller.startGame()
        controller.update(0.1)

        window.dispatch(FocusEvent(FocusEvent.Type.BLUR))
        val pausedSnapshot = controller.snapshot()
        controller.update(30.0)

        assertTrue(controller.isPaused)
        assertEquals(pausedSnapshot, controller.snapshot())

        window.dispatch(FocusEvent(FocusEvent.Type.FOCUS))
        window.dispatch(ResumeEvent())
        assertTrue(controller.isPaused)

        controller.resumeGame()
        assertTrue(controller.isGameRunning)
    }

    @Test
    fun minimizingOrBackgroundingPausesAnActiveRun() {
        val controller = GameController(gameAreaWidth = 1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        controller.startGame()

        window.dispatchPauseEvent()

        assertTrue(controller.isPaused)
        assertFalse(controller.isGameRunning)
    }

    @Test
    fun losingFocusInTheMenuDoesNotStartOrPauseARun() {
        val controller = GameController(gameAreaWidth = 1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)

        window.dispatch(FocusEvent(FocusEvent.Type.BLUR))
        window.dispatchPauseEvent()

        assertTrue(controller.isMenuVisible)
        assertFalse(controller.isPaused)
        assertEquals(0, controller.runNumber)
    }
}
