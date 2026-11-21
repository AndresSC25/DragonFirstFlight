package com.redcrow.dragonsfirstflight

import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent

internal class IntroSkipKeyboard(
    private val isActive: () -> Boolean,
    private val canSkip: () -> Boolean,
    private val onFocus: () -> Unit,
    private val onSkip: (Int) -> Unit,
) : KeyEventDispatcher {
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.id != KeyEvent.KEY_PRESSED || !isActive()) return false
        when (event.keyCode) {
            KeyEvent.VK_TAB -> onFocus()
            KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE -> {
                if (!canSkip()) return false
                onSkip(event.keyCode)
            }
            else -> return false
        }
        event.consume()
        return true
    }
}
