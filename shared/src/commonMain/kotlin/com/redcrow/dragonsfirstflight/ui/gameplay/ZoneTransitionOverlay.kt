package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Views

internal class ZoneTransitionOverlay private constructor(
    private val shade: SolidRect,
) {
    fun update(snapshot: GameSnapshot) {
        if (!snapshot.isZoneTransitioning) {
            shade.visible = false
            return
        }

        val progress = snapshot.zoneTransitionProgress
        val flashIndex = (progress / FLASH_PERIOD).toInt()
        shade.color =
            if (flashIndex % 2 == 0) {
                Colors.WHITE
            } else {
                Colors.BLACK
            }
        shade.alpha =
            when {
                progress < FADE_OUT_START -> 1.0
                else ->
                    (
                        1.0 - progress
                    ) / (1.0 - FADE_OUT_START)
            }.coerceIn(0.0, 1.0)
        shade.visible = shade.alpha > 0.0
    }

    companion object {
        private const val FLASH_PERIOD: Double = 0.12
        private const val FADE_OUT_START: Double = 0.72

        fun create(
            container: Container,
            views: Views,
        ): ZoneTransitionOverlay =
            ZoneTransitionOverlay(
                shade =
                    container.createResponsiveShade(
                        views = views,
                        color = Colors.WHITE,
                        shadeAlpha = 0.0,
                    ),
            )
    }
}
