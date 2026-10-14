package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

internal class PlaceholderObstacleView private constructor(
    private val obstacle: SolidRect,
    private val label: Text
) {
    fun update(snapshot: GameSnapshot) {
        obstacle.x = snapshot.obstacleX
        obstacle.y = snapshot.obstacleY

        label.x =
            obstacle.x +
                (snapshot.obstacleSize - label.width) / 2.0

        label.y =
            obstacle.y +
                (snapshot.obstacleSize - label.height) / 2.0
    }

    companion object {
        fun create(container: Container): PlaceholderObstacleView {
            val obstacle = container.solidRect(
                width = GameEngineConfig.OBSTACLE_SIZE,
                height = GameEngineConfig.OBSTACLE_SIZE,
                color = Colors["#E53935"]
            )

            val label = container.text(
                text = "X",
                textSize = 36.0,
                color = Colors.WHITE
            )

            return PlaceholderObstacleView(
                obstacle = obstacle,
                label = label
            )
        }
    }
}
