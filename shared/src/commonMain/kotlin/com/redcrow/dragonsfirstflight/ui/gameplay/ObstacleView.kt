package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.Obstacle
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.View
import korlibs.korge.view.Views
import korlibs.korge.view.image
import kotlin.math.min

private const val HORIZONTAL_FADE_DISTANCE: Double = 96.0

private data class RenderedObstacle(
    val view: View,
    val displayWidth: Int,
    val displayHeight: Int,
    val animatedSprite: AnimatedSpriteView?
)

internal class ObstacleView private constructor(
    private val container: Container,
    private val obstacleBitmaps: Map<String, Bitmap>,
    private val views: Views,
    private val animatedObstacleConfigs: Map<String, AnimatedSpriteConfig>
) {
    private val viewsById = mutableMapOf<Int, RenderedObstacle>()

    fun update(snapshot: GameSnapshot, isPaused: Boolean) {
        val activeIds = snapshot.obstacles.mapTo(mutableSetOf()) { it.id }
        viewsById.keys.toList()
            .filter { it !in activeIds }
            .forEach { id -> viewsById.remove(id)?.view?.removeFromParent() }

        val hasFinished = snapshot.status != GameStatus.RUNNING
        snapshot.obstacles.forEach { obstacle ->
            val rendered = viewsById.getOrPut(obstacle.id) {
                createRenderedObstacle(obstacle.spritePath)
            }
            val visible = obstacle.id !in snapshot.destroyedObstacleIds &&
                !hasFinished
            rendered.view.visible = visible
            rendered.animatedSprite?.update(
                shouldAnimate = visible && !isPaused
            )
            updateView(rendered, obstacle)
            if (visible) {
                rendered.view.alpha = calculateHorizontalAlpha(obstacle)
            }
        }
    }

    private fun createRenderedObstacle(spritePath: String): RenderedObstacle {
        val bitmap = obstacleBitmaps.getValue(spritePath)
        val config = animatedObstacleConfigs[spritePath]
        if (config == null) {
            return RenderedObstacle(
                view = container.image(bitmap) {
                    smoothing = false
                    alpha = 0.0
                },
                displayWidth = bitmap.width,
                displayHeight = bitmap.height,
                animatedSprite = null
            )
        }

        val animatedSprite = AnimatedSpriteView.create(
            container = container,
            bitmap = bitmap,
            config = config
        )
        animatedSprite.view.alpha = 0.0
        return RenderedObstacle(
            view = animatedSprite.view,
            displayWidth = config.frameWidth,
            displayHeight = config.frameHeight,
            animatedSprite = animatedSprite
        )
    }

    private fun updateView(rendered: RenderedObstacle, obstacle: Obstacle) {
        rendered.view.x = obstacle.x
        rendered.view.y = obstacle.y
        rendered.view.scaleX = obstacle.size / rendered.displayWidth.toDouble()
        rendered.view.scaleY = obstacle.size / rendered.displayHeight.toDouble()
    }

    private fun calculateHorizontalAlpha(obstacle: Obstacle): Double {
        val leftBoundary = views.actualVirtualLeft.toDouble()
        val rightBoundary = leftBoundary + views.actualVirtualWidth.toDouble()
        val obstacleRight = obstacle.x + obstacle.size
        val leftAlpha = ((obstacleRight - leftBoundary) /
            HORIZONTAL_FADE_DISTANCE).coerceIn(0.0, 1.0)
        val rightAlpha = ((rightBoundary - obstacle.x) /
            HORIZONTAL_FADE_DISTANCE).coerceIn(0.0, 1.0)
        return min(leftAlpha, rightAlpha)
    }

    companion object {
        fun create(
            container: Container,
            obstacleBitmaps: Map<String, Bitmap>,
            views: Views,
            animatedObstacleConfigs: Map<String, AnimatedSpriteConfig> =
                emptyMap()
        ): ObstacleView {
            require(obstacleBitmaps.isNotEmpty()) {
                "At least one obstacle bitmap is required."
            }
            require(obstacleBitmaps.values.all { it.width > 0 && it.height > 0 }) {
                "Obstacle bitmaps must have positive dimensions."
            }
            require(HORIZONTAL_FADE_DISTANCE > 0.0) {
                "Obstacle fade distance must be greater than zero."
            }
            return ObstacleView(
                container = container,
                obstacleBitmaps = obstacleBitmaps,
                views = views,
                animatedObstacleConfigs = animatedObstacleConfigs
            )
        }
    }
}
