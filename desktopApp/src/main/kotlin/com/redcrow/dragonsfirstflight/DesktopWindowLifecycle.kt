package com.redcrow.dragonsfirstflight

import korlibs.event.FocusEvent
import korlibs.render.GameWindow
import korlibs.render.awt.AwtGameWindow
import korlibs.render.awt.NewAwtGameWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

internal suspend fun installDesktopWindowLifecycle(window: GameWindow) {
    withContext(Dispatchers.Swing) {
        val frame =
            when (window) {
                is AwtGameWindow -> window.frame
                is NewAwtGameWindow -> window.frame
                else -> return@withContext
            }
        val listener =
            object : WindowAdapter() {
                private fun pauseGame() {
                    // Apply the state change on KorGE's update thread.
                    window.queue { window.dispatch(FocusEvent(FocusEvent.Type.BLUR)) }
                }

                override fun windowLostFocus(event: WindowEvent) = pauseGame()

                override fun windowGainedFocus(event: WindowEvent) {
                    window.queue { window.dispatch(FocusEvent(FocusEvent.Type.FOCUS)) }
                }

                override fun windowDeactivated(event: WindowEvent) = pauseGame()

                override fun windowIconified(event: WindowEvent) = pauseGame()

                override fun windowClosed(event: WindowEvent) {
                    frame.removeWindowFocusListener(this)
                    frame.removeWindowListener(this)
                }
            }
        frame.addWindowFocusListener(listener)
        frame.addWindowListener(listener)
    }
}
