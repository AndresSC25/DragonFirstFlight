package com.redcrow.dragonsfirstflight.ui.gameplay

import kotlin.test.Test
import kotlin.test.assertTrue

class ScrollingBackgroundTest {
    @Test
    fun tilesCoverViewportAtEveryScrollOffset() {
        val viewportWidths = listOf(960.0, 1280.0, 1680.0, 2560.0)
        val tileWidths = listOf(640.0, 1279.4, 2160.0)

        for (viewportWidth in viewportWidths) {
            for (tileWidth in tileWidths) {
                for (tilesPerPeriod in 1..2) {
                    val periodWidth = tileWidth * tilesPerPeriod
                    val tileCount =
                        tilesToCover(
                            viewportWidth = viewportWidth,
                            tileWidth = tileWidth,
                            tilesPerPeriod = tilesPerPeriod,
                        )

                    for (offset in listOf(0.0, periodWidth / 2.0, periodWidth - 0.001)) {
                        val stripRight = -offset + tileCount * tileWidth

                        assertTrue(
                            stripRight >= viewportWidth,
                            "Gap at viewport=$viewportWidth tile=$tileWidth " +
                                "period=$tilesPerPeriod offset=$offset",
                        )
                    }
                }
            }
        }
    }
}
