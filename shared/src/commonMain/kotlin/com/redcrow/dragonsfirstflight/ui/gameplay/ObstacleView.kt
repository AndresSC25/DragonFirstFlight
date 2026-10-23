package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.Obstacle
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image
import kotlin.math.min

private const val HORIZONTAL_FADE_DISTANCE: Double = 96.0

internal class ObstacleView private constructor(
    private val container: Container,
    private val obstacleBitmaps: Map<String, Bitmap>
) {
    private val viewsById =
        mutableMapOf<Int, Image>()

    fun update(snapshot: GameSnapshot) {
        val activeIds =
            snapshot.obstacles.mapTo(mutableSetOf()) { obstacle ->
                obstacle.id
            }

        viewsById.keys
            .toList()
            .filter { id ->
                id !in activeIds
            }
            .forEach { id ->
                viewsById
                    .remove(id)
                    ?.removeFromParent()
            }

        snapshot.obstacles.forEach { obstacle ->
            val view =
                viewsById.getOrPut(obstacle.id) {
                    createObstacleImage(obstacle)
                }

            updateView(
                view = view,
                obstacle = obstacle,
                isDestroyed =
                    obstacle.id in snapshot.destroyedObstacleIds
            )
        }
    }

    private fun createObstacleImage(
        obstacle: Obstacle
    ): Image {
        val bitmap =
            obstacleBitmaps.getValue(
                obstacle.spritePath
            )

        return container.image(
            texture = bitmap
        ) {
            smoothing = false
            alpha = 0.0
        }
    }

    private fun updateView(
        view: Image,
        obstacle: Obstacle,
        isDestroyed: Boolean
    ) {
        val bitmap =
            obstacleBitmaps.getValue(
                obstacle.spritePath
            )

        view.x = obstacle.x
        view.y = obstacle.y

        view.scaleX =
            obstacle.size /
                bitmap.width.toDouble()

        view.scaleY =
            obstacle.size /
                bitmap.height.toDouble()

        view.visible = !isDestroyed

        if (!view.visible) {
            return
        }

        view.alpha =
            calculateHorizontalAlpha(obstacle)
    }

    private fun calculateHorizontalAlpha(
        obstacle: Obstacle
    ): Double {
        val obstacleRight =
            obstacle.x + obstacle.size

        val leftEdgeAlpha =
            (
                obstacleRight /
                    HORIZONTAL_FADE_DISTANCE
            ).coerceIn(
                minimumValue = 0.0,
                maximumValue = 1.0
            )

        val distanceFromRightEdge =
            DragonGameConfig.GAME_AREA_WIDTH -
                obstacle.x

        val rightEdgeAlpha =
            (
                distanceFromRightEdge /
                    HORIZONTAL_FADE_DISTANCE
            ).coerceIn(
                minimumValue = 0.0,
                maximumValue = 1.0
            )

        return min(
            leftEdgeAlpha,
            rightEdgeAlpha
        )
    }

    companion object {
        fun create(
            container: Container,
            obstacleBitmaps: Map<String, Bitmap>
        ): ObstacleView {
            require(obstacleBitmaps.isNotEmpty()) {
                "At least one obstacle bitmap is required."
            }

            require(
                obstacleBitmaps.values.all { bitmap ->
                    bitmap.width > 0 &&
                        bitmap.height > 0
                }
            ) {
                "Obstacle bitmaps must have positive dimensions."
            }

            require(HORIZONTAL_FADE_DISTANCE > 0.0) {
                "HORIZONTAL_FADE_DISTANCE must be greater than zero."
            }

            return ObstacleView(
                container = container,
                obstacleBitmaps = obstacleBitmaps
            )
        }
    }
}