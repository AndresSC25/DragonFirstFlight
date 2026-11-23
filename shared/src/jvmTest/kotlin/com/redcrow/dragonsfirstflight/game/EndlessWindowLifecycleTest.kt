package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import korlibs.event.FocusEvent
import korlibs.event.ResumeEvent
import korlibs.render.GameWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EndlessWindowLifecycleTest {
    @Test
    fun losingFocusFreezesEndlessUntilThePlayerExplicitlyResumes() {
        val controller = GameController(1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        controller.startEndless(GameZone.BOSS, GameDifficulty.HARD)
        controller.update(0.05)
        window.dispatch(FocusEvent(FocusEvent.Type.BLUR))
        val paused = controller.snapshot()
        controller.update(120.0)
        assertTrue(controller.isPaused)
        assertEquals(paused, controller.snapshot())
        window.dispatch(FocusEvent(FocusEvent.Type.FOCUS))
        window.dispatch(ResumeEvent())
        controller.update(30.0)
        assertTrue(controller.isPaused)
        assertEquals(paused, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertTrue(controller.snapshot().endlessElapsedSeconds > paused.endlessElapsedSeconds)
    }

    @Test
    fun backgroundingDuringTheVictoryTransitionFreezesItUntilResume() {
        val controller = GameController(1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        controller.startGame()
        controller.update(GameEngineConfig.TIME_TO_WIN_SECONDS)
        controller.continueEndless()
        controller.update(0.05)
        window.dispatchPauseEvent()
        val paused = controller.snapshot()
        assertTrue(paused.isZoneTransitioning)
        controller.update(120.0)
        window.dispatch(ResumeEvent())
        assertTrue(controller.isPaused)
        assertEquals(paused, controller.snapshot())
        controller.resumeGame()
        controller.update(0.05)
        assertTrue(controller.snapshot().zoneTransitionProgress > paused.zoneTransitionProgress)
    }

    @Test
    fun losingFocusInMenusOrSetupDoesNotStartOrPauseAGame() {
        val controller = GameController(1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        window.dispatch(FocusEvent(FocusEvent.Type.BLUR))
        assertTrue(controller.isMenuVisible)
        assertFalse(controller.isPaused)
        controller.openEndlessSetup()
        window.dispatchPauseEvent()
        assertTrue(controller.isEndlessSetupVisible)
        assertFalse(controller.isPaused)
        assertEquals(0, controller.runNumber)
    }

    @Test
    fun normalRunsAlsoPauseWhenTheWindowBecomesInactive() {
        val controller = GameController(1280.0)
        val window = GameWindow()
        pauseGameWhenWindowInactive(controller, window)
        controller.startGame()
        window.dispatchPauseEvent()
        assertTrue(controller.isPaused)
        assertFalse(controller.isGameRunning)
    }
}
