package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameMode
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

private const val PANEL_WIDTH = 440.0
private const val PANEL_HEIGHT = 190.0
private const val CONTENT_WIDTH = 340.0
private const val CONTENT_HEIGHT = 136.0

internal class ZoneTransitionOverlay private constructor(
    private val shade: SolidRect,
    private val announcement: Container,
    private val title: Text,
    private val subtitle: Text,
) {
    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean = false,
    ) {
        if (!snapshot.isZoneTransitioning || isPaused) {
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
            if (snapshot.mode == GameMode.ENDLESS) {
                "ENDLESS"
            } else {
                when (snapshot.zone) {
                    GameZone.ZONE_ONE -> "THE COURTYARD"
                    GameZone.ZONE_TWO -> "THE TOWERS"
                    GameZone.ZONE_THREE -> "THE WILDS"
                    GameZone.BOSS -> "FINAL ASSAULT"
                }
            }
        val zoneSubtitle =
            if (snapshot.mode == GameMode.ENDLESS) {
                "THE FLIGHT CONTINUES"
            } else {
                when (snapshot.zone) {
                    GameZone.ZONE_ONE -> "Take flight from the fortress"
                    GameZone.ZONE_TWO -> "Climb beyond the fortress walls"
                    GameZone.ZONE_THREE -> "Escape through the outer forest"
                    GameZone.BOSS -> "Break through the siege"
                }
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
        announcement.y =
            (DragonGameConfig.VIRTUAL_SIZE.height - PANEL_HEIGHT) / 2.0 +
            entranceOffset(progress)
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
            progress < FADE_IN_END -> smoothStep(progress / FADE_IN_END)
            progress <= FADE_OUT_START -> 1.0
            else ->
                1.0 -
                    smoothStep(
                        (progress - FADE_OUT_START) /
                            (1.0 - FADE_OUT_START),
                    )
        }.coerceIn(0.0, 1.0)

    private fun entranceOffset(progress: Double): Double {
        val visibleProgress =
            when {
                progress < FADE_IN_END -> smoothStep(progress / FADE_IN_END)
                progress <= FADE_OUT_START -> 1.0
                else ->
                    1.0 -
                        smoothStep(
                            (progress - FADE_OUT_START) /
                                (1.0 - FADE_OUT_START),
                        )
            }
        return (1.0 - visibleProgress) * ENTRANCE_OFFSET_Y
    }

    private fun smoothStep(value: Double): Double {
        val normalized = value.coerceIn(0.0, 1.0)
        return normalized * normalized * (3.0 - 2.0 * normalized)
    }

    companion object {
        const val TRANSITION_MIDPOINT = 0.50
        private const val MAX_SHADE_ALPHA = 0.68
        private const val FADE_IN_END = 0.22
        private const val FADE_OUT_START = 0.74
        private const val ENTRANCE_OFFSET_Y = 18.0

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
                        alpha = 0.72
                    }

                    image(texture = panelBitmap) {
                        scaleX = PANEL_WIDTH / panelBitmap.width.toDouble()
                        scaleY = PANEL_HEIGHT / panelBitmap.height.toDouble()
                        smoothing = false
                    }

                    title =
                        text(
                            text = "THE TOWERS",
                            textSize = 36.0,
                            color = Colors["#F8D66D"],
                        )
                    title.x = (PANEL_WIDTH - title.width) / 2.0
                    title.y = 50.0

                    subtitle =
                        text(
                            text = "Climb beyond the fortress walls",
                            textSize = 18.0,
                            color = Colors.WHITE,
                        )
                    subtitle.x = (PANEL_WIDTH - subtitle.width) / 2.0
                    subtitle.y = 111.0
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
