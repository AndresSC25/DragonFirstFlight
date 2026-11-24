package com.redcrow.dragonsfirstflight

import java.awt.Canvas
import java.awt.event.KeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntroSkipKeyboardTest {
    @Test
    fun tabRevealsFocusWithoutSkippingWhileTheVideoLoads() {
        var focused = false
        var skipped = false
        val keyboard =
            IntroSkipKeyboard(
                isActive = { true },
                canSkip = { false },
                onFocus = { focused = true },
                onSkip = { skipped = true },
            )

        assertTrue(keyboard.dispatchKeyEvent(key(KeyEvent.VK_TAB)))
        assertTrue(focused)
        assertFalse(keyboard.dispatchKeyEvent(key(KeyEvent.VK_ENTER)))
        assertFalse(skipped)
    }

    @Test
    fun backgroundWindowsKeepTheirKeyboardInput() {
        val keyboard =
            IntroSkipKeyboard(
                isActive = { false },
                canSkip = { true },
                onFocus = { error("An inactive intro cannot take keyboard focus") },
                onSkip = { error("An inactive intro cannot handle another window's keys") },
            )
        for (code in listOf(KeyEvent.VK_TAB, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE)) {
            val event = key(code)
            assertFalse(keyboard.dispatchKeyEvent(event))
            assertFalse(event.isConsumed)
        }
    }

    @Test
    fun activationKeysSkipOnceAndIgnoreReleaseEvents() {
        for (code in listOf(KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE)) {
            var finished = false
            var skips = 0
            val keyboard =
                IntroSkipKeyboard(
                    isActive = { !finished },
                    canSkip = { true },
                    onFocus = {},
                    onSkip = {
                        skips++
                        finished = true
                    },
                )

            assertFalse(keyboard.dispatchKeyEvent(key(code, KeyEvent.KEY_RELEASED)))
            assertTrue(keyboard.dispatchKeyEvent(key(code)))
            assertFalse(keyboard.dispatchKeyEvent(key(code)))
            assertEquals(1, skips)
        }
    }

    private fun key(
        code: Int,
        type: Int = KeyEvent.KEY_PRESSED,
    ): KeyEvent = KeyEvent(Canvas(), type, 0L, 0, code, KeyEvent.CHAR_UNDEFINED)
}
