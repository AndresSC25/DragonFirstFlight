package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.Obstacle
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

internal class ObstacleView private constructor(
    private val container: Container,
    private val obstacleBitmaps: Map<String, Bitmap>
) {
    private val viewsById = mutableMapOf<Int, Image>()

    fun update(snapshot: GameSnapshot) {
        val activeIds = snapshot.obstacles.mapTo(mutableSetOf()) { it.id }
        viewsById.keys.toList()
            .filter { it !in activeIds }
            .forEach { id ->
                viewsById.remove(id)?.removeFromParent()
            }

        snapshot.obstacles.forEach { obstacle ->
            val view = viewsById.getOrPut(obstacle.id) {
                container.image(obstacleBitmaps.getValue(obstacle.spritePath)).also {
                    it.smoothing = false
                }
            }
            updateView(view, obstacle)
        }
    }

    private fun updateView(view: Image, obstacle: Obstacle) {
        val bitmap = obstacleBitmaps.getValue(obstacle.spritePath)
        view.x = obstacle.x
        view.y = obstacle.y
        view.scaleX = obstacle.size / bitmap.width.toDouble()
        view.scaleY = obstacle.size / bitmap.height.toDouble()
    }

    companion object {
        fun create(
            container: Container,
            obstacleBitmaps: Map<String, Bitmap>
        ): ObstacleView {
            require(obstacleBitmaps.isNotEmpty()) {
                "At least one obstacle bitmap is required."
            }
            require(obstacleBitmaps.values.all { it.width > 0 && it.height > 0 }) {
                "Obstacle bitmaps must have positive dimensions."
            }
            return ObstacleView(container, obstacleBitmaps)
        }
    }
}
