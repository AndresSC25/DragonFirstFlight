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
            shade.alpha = 0.0
            shade.visible = false
            return
        }

        val progress = snapshot.zoneTransitionProgress.coerceIn(0.0, 1.0)
        shade.color = Colors.BLACK
        shade.alpha = transitionAlpha(progress)
        shade.visible = shade.alpha > 0.0
    }

    private fun transitionAlpha(progress: Double): Double =
        when {
            progress < FADE_IN_END ->
                MAX_SHADE_ALPHA * (progress / FADE_IN_END)

            progress <= FADE_OUT_START ->
                MAX_SHADE_ALPHA

            else ->
                MAX_SHADE_ALPHA *
                    ((1.0 - progress) / (1.0 - FADE_OUT_START))
        }.coerceIn(0.0, MAX_SHADE_ALPHA)

    companion object {
        private const val FADE_IN_END: Double = 0.30
        private const val FADE_OUT_START: Double = 0.65
        private const val MAX_SHADE_ALPHA: Double = 0.68

        fun create(
            container: Container,
            views: Views,
        ): ZoneTransitionOverlay =
            ZoneTransitionOverlay(
                shade =
                    container.createResponsiveShade(
                        views = views,
                        color = Colors.BLACK,
                        shadeAlpha = 0.0,
                    ),
            )
    }
}
