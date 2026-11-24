package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTransitionOverlayTest {
    @Test
    fun backgroundBlendStaysAtEndpointsOutsideFadeWindow() {
        assertEquals(0.0, ZoneTransitionOverlay.backgroundBlend(0.0))
        assertEquals(0.0, ZoneTransitionOverlay.backgroundBlend(0.30))
        assertEquals(1.0, ZoneTransitionOverlay.backgroundBlend(0.70))
        assertEquals(1.0, ZoneTransitionOverlay.backgroundBlend(1.0))
    }

    @Test
    fun backgroundBlendIncreasesSmoothlyAtSixtyFramesPerSecond() {
        val frameCount = (GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS * 60).toInt()
        var previousBlend = ZoneTransitionOverlay.backgroundBlend(0.0)

        for (frame in 1..frameCount) {
            val blend = ZoneTransitionOverlay.backgroundBlend(frame.toDouble() / frameCount)

            assertTrue(blend >= previousBlend, "Background blend decreased at frame $frame")
            assertTrue(blend - previousBlend <= 0.05, "Background blend changed by more than 0.05 at frame $frame")

            previousBlend = blend
        }
    }
}
