package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val PANEL_WIDTH: Double = 500.0
private const val PANEL_HEIGHT: Double = 240.0
private const val CONTENT_WIDTH: Double = 390.0
private const val CONTENT_HEIGHT: Double = 181.0

internal class ZoneTransitionOverlay private constructor(
    private val shade: SolidRect,
    private val announcement: Container,
    private val title: Text,
    private val subtitle: Text,
) {
    fun update(snapshot: GameSnapshot) {
        if (!snapshot.isZoneTransitioning) {
            shade.alpha = 0.0
            shade.visible = false
            announcement.alpha = 0.0
            announcement.visible = false
            return
        }

        val progress = snapshot.zoneTransitionProgress.coerceIn(0.0, 1.0)
        shade.color = Colors.BLACK
        shade.alpha = transitionAlpha(progress)
        shade.visible = shade.alpha > 0.0

        val zoneText =
            when (snapshot.zone) {
                GameZone.ZONE_ONE -> "ZONE I"
                GameZone.ZONE_TWO -> "ZONE II"
                GameZone.ZONE_THREE -> "ZONE III"
                GameZone.BOSS -> "ZONE IV"
            }
        val zoneSubtitle =
            when (snapshot.zone) {
                GameZone.ZONE_ONE -> "FORTRESS COURTYARD"
                GameZone.ZONE_TWO -> "TOWERS AND WALLS"
                GameZone.ZONE_THREE -> "OUTER FOREST"
                GameZone.BOSS -> "Boss"
            }

        if (title.text != zoneText) {
            title.text = zoneText
            title.x = (PANEL_WIDTH - title.width) / 2.0
        }
        if (subtitle.text != zoneSubtitle) {
            subtitle.text = zoneSubtitle
            subtitle.x = (PANEL_WIDTH - subtitle.width) / 2.0
        }

        announcement.alpha = announcementAlpha(progress)
        announcement.visible = announcement.alpha > 0.0
    }

    private fun transitionAlpha(progress: Double): Double {
        val normalizedProgress =
            if (progress <= TRANSITION_MIDPOINT) {
                progress / TRANSITION_MIDPOINT
            } else {
                (1.0 - progress) / (1.0 - TRANSITION_MIDPOINT)
            }.coerceIn(0.0, 1.0)

        return MAX_SHADE_ALPHA * smoothStep(normalizedProgress)
    }

    private fun announcementAlpha(progress: Double): Double =
        when {
            progress < ANNOUNCEMENT_FADE_IN_START -> 0.0
            progress < ANNOUNCEMENT_FADE_IN_END ->
                smoothStep(
                    (progress - ANNOUNCEMENT_FADE_IN_START) /
                        (ANNOUNCEMENT_FADE_IN_END - ANNOUNCEMENT_FADE_IN_START),
                )
            progress <= ANNOUNCEMENT_FADE_OUT_START -> 1.0
            progress < ANNOUNCEMENT_FADE_OUT_END ->
                1.0 -
                    smoothStep(
                        (progress - ANNOUNCEMENT_FADE_OUT_START) /
                            (ANNOUNCEMENT_FADE_OUT_END - ANNOUNCEMENT_FADE_OUT_START),
                    )
            else -> 0.0
        }.coerceIn(0.0, 1.0)

    private fun smoothStep(value: Double): Double {
        val progress = value.coerceIn(0.0, 1.0)
        return progress * progress * (3.0 - 2.0 * progress)
    }

    companion object {
        const val TRANSITION_MIDPOINT: Double = 0.50
        private const val MAX_SHADE_ALPHA: Double = 0.94
        private const val ANNOUNCEMENT_FADE_IN_START: Double = 0.12
        private const val ANNOUNCEMENT_FADE_IN_END: Double = 0.24
        private const val ANNOUNCEMENT_FADE_OUT_START: Double = 0.78
        private const val ANNOUNCEMENT_FADE_OUT_END: Double = 0.94

        fun create(
            container: Container,
            views: Views,
            panelBitmap: Bitmap,
        ): ZoneTransitionOverlay {
            require(panelBitmap.width > 0 && panelBitmap.height > 0) {
                "Zone transition panel bitmap must have positive dimensions."
            }

            val shade =
                container.createResponsiveShade(
                    views = views,
                    color = Colors.BLACK,
                    shadeAlpha = 0.0,
                )

            lateinit var title: Text
            lateinit var subtitle: Text
            val announcement =
                container.container {
                    x =
                        (DragonGameConfig.VIRTUAL_SIZE.width - PANEL_WIDTH) / 2.0
                    y =
                        (DragonGameConfig.VIRTUAL_SIZE.height - PANEL_HEIGHT) / 2.0
                    alpha = 0.0
                    visible = false

                    solidRect(
                        width = CONTENT_WIDTH,
                        height = CONTENT_HEIGHT,
                        color = Colors["#101827"],
                    ) {
                        x = (PANEL_WIDTH - CONTENT_WIDTH) / 2.0
                        y = (PANEL_HEIGHT - CONTENT_HEIGHT) / 2.0
                        alpha = 0.78
                    }

                    image(texture = panelBitmap) {
                        scaleX = PANEL_WIDTH / panelBitmap.width.toDouble()
                        scaleY = PANEL_HEIGHT / panelBitmap.height.toDouble()
                        smoothing = false
                    }

                    title =
                        text(
                            text = "ZONE II",
                            textSize = 44.0,
                            color = Colors["#F8D66D"],
                        )
                    title.x = (PANEL_WIDTH - title.width) / 2.0
                    title.y = 65.0

                    subtitle =
                        text(
                            text = "TOWERS AND WALLS",
                            textSize = 20.0,
                            color = Colors.WHITE,
                        )
                    subtitle.x = (PANEL_WIDTH - subtitle.width) / 2.0
                    subtitle.y = 135.0
                }

            return ZoneTransitionOverlay(
                shade = shade,
                announcement = announcement,
                title = title,
                subtitle = subtitle,
            )
        }
    }
}
