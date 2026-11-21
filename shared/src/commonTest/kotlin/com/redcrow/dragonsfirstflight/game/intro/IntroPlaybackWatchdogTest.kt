package com.redcrow.dragonsfirstflight.game.intro

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntroPlaybackWatchdogTest {
    @Test
    fun decoderThatNeverStartsTimesOut() {
        val watchdog = IntroPlaybackWatchdog()
        assertFalse(watchdog.hasTimedOut(14.0, false, null))
        assertTrue(watchdog.hasTimedOut(1.0, false, null))
    }

    @Test
    fun playbackThatStopsAdvancingTimesOut() {
        val watchdog = IntroPlaybackWatchdog()
        assertFalse(watchdog.hasTimedOut(0.1, false, 2.0))
        assertFalse(watchdog.hasTimedOut(4.0, false, 2.0))
        assertTrue(watchdog.hasTimedOut(1.0, false, 2.0))
    }

    @Test
    fun backgroundTimeDoesNotConsumeStartupOrPlaybackTimeout() {
        val watchdog = IntroPlaybackWatchdog()
        assertFalse(watchdog.hasTimedOut(120.0, true, null))
        assertFalse(watchdog.hasTimedOut(14.0, false, null))
        assertFalse(watchdog.hasTimedOut(0.1, false, 1.0))
        assertFalse(watchdog.hasTimedOut(120.0, true, 1.0))
        assertFalse(watchdog.hasTimedOut(4.0, false, 1.0))
    }

    @Test
    fun resumedProgressResetsTheStallTimer() {
        val watchdog = IntroPlaybackWatchdog()
        assertFalse(watchdog.hasTimedOut(0.1, false, 1.0))
        assertFalse(watchdog.hasTimedOut(4.5, false, 1.0))
        assertFalse(watchdog.hasTimedOut(0.1, false, 1.1))
        assertFalse(watchdog.hasTimedOut(4.5, false, 1.1))
    }

    @Test
    fun invalidElapsedTimeDoesNotCreateAFalseTimeout() {
        val watchdog = IntroPlaybackWatchdog()
        assertFalse(watchdog.hasTimedOut(Double.NaN, false, null))
        assertFalse(watchdog.hasTimedOut(-1.0, false, null))
        assertFalse(watchdog.hasTimedOut(14.0, false, null))
    }
}
