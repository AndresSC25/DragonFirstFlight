package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val WARNING_SIZE = 80.0
private const val WARNING_RIGHT_MARGIN = 28.0
private const val WARNING_LINE_WIDTH = 48.0
private const val WARNING_LINE_HEIGHT = 5.0
private const val WARNING_SYMBOL_SIZE = 46.0

internal class RushWarningView private constructor(
    private val container: Container,
    private val plate: SolidRect,
    private val upperLine: SolidRect,
    private val lowerLine: SolidRect,
    private val symbol: Text,
    private val views: Views,
) {
    fun update(snapshot: GameSnapshot) {
        val warningObstacle =
            snapshot.obstacles
                .filter { obstacle ->
                    obstacle.horizontalBehavior.isRush &&
                        obstacle.rushWarningRemainingSeconds > 0.0
                }.minByOrNull { obstacle -> obstacle.id }

        val shouldShow =
            warningObstacle != null &&
                snapshot.status == GameStatus.RUNNING &&
                !snapshot.isZoneTransitioning

        container.visible = shouldShow
        if (warningObstacle == null || !shouldShow) return

        val viewportLeft = views.actualVirtualLeft.toDouble()
        val viewportRight = viewportLeft + views.actualVirtualWidth.toDouble()
        val warningX = viewportRight - WARNING_RIGHT_MARGIN - WARNING_SIZE
        val warningY =
            (warningObstacle.y + warningObstacle.height / 2.0 - WARNING_SIZE / 2.0)
                .coerceIn(
                    GameEngineConfig.DRAGON_MIN_Y,
                    GameEngineConfig.GAME_AREA_HEIGHT - WARNING_SIZE,
                )

        container.x = warningX
        container.y = warningY

        val progress =
            1.0 -
                warningObstacle.rushWarningRemainingSeconds /
                GameEngineConfig.RUSH_WARNING_DURATION_SECONDS
        container.alpha = 0.82 + progress.coerceIn(0.0, 1.0) * 0.18

        val urgency = progress.coerceIn(0.0, 1.0)
        plate.alpha = 0.52 + urgency * 0.30
        upperLine.alpha = 0.55 + urgency * 0.35
        lowerLine.alpha = 0.55 + urgency * 0.35
    }

    companion object {
        fun create(
            parent: Container,
            views: Views,
        ): RushWarningView {
            val warningContainer =
                parent.container {
                    visible = false
                }

            val plate =
                warningContainer.solidRect(
                    width = WARNING_SIZE,
                    height = WARNING_SIZE,
                    color = Colors["#9E1B1B"],
                ) {
                    alpha = 0.70
                }

            val upperLine =
                warningContainer.solidRect(
                    width = WARNING_LINE_WIDTH,
                    height = WARNING_LINE_HEIGHT,
                    color = Colors["#FFB347"],
                ) {
                    x = -WARNING_LINE_WIDTH - 8.0
                    y = WARNING_SIZE / 2.0 - 10.0
                }

            val lowerLine =
                warningContainer.solidRect(
                    width = WARNING_LINE_WIDTH,
                    height = WARNING_LINE_HEIGHT,
                    color = Colors["#FFB347"],
                ) {
                    x = -WARNING_LINE_WIDTH - 8.0
                    y = WARNING_SIZE / 2.0 + 6.0
                }

            val symbol =
                warningContainer.text(
                    text = "!",
                    textSize = WARNING_SYMBOL_SIZE,
                    color = Colors.WHITE,
                )
            symbol.x = (WARNING_SIZE - symbol.width) / 2.0
            symbol.y = (WARNING_SIZE - symbol.height) / 2.0 + 3.0

            return RushWarningView(
                container = warningContainer,
                plate = plate,
                upperLine = upperLine,
                lowerLine = lowerLine,
                symbol = symbol,
                views = views,
            )
        }
    }
}
