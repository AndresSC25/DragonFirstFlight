package com.redcrow.dragonsfirstflight

import com.redcrow.dragonsfirstflight.game.launchDragonGame
import korlibs.render.GameWindowCreationConfig
import korlibs.render.awt.BaseAwtGameWindow
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

private const val MINIMUM_WINDOW_WIDTH: Int = 1280
private const val MINIMUM_WINDOW_HEIGHT: Int = 720
private const val GAME_ASPECT_RATIO: Double = 16.0 / 9.0

suspend fun main() {
    launchDragonGame(
        windowCreationConfig =
            GameWindowCreationConfig(
                fullscreen = false,
                resizable = true,
            ),
        onWindowCreated = { gameWindow ->
            val awtWindow = gameWindow as? BaseAwtGameWindow
            awtWindow?.window?.let { window ->
                window.minimumSize =
                    Dimension(
                        MINIMUM_WINDOW_WIDTH,
                        MINIMUM_WINDOW_HEIGHT,
                    )

                var previousSize = window.size
                var correctingSize = false
                window.addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(event: ComponentEvent) {
                            if (correctingSize) {
                                return
                            }

                            val currentSize = event.component.size
                            if (currentSize == previousSize) {
                                return
                            }

                            val insets = window.insets
                            val horizontalInsets = insets.left + insets.right
                            val verticalInsets = insets.top + insets.bottom
                            val currentContentWidth =
                                (currentSize.width - horizontalInsets).coerceAtLeast(1)
                            val currentContentHeight =
                                (currentSize.height - verticalInsets).coerceAtLeast(1)
                            val previousContentWidth =
                                (previousSize.width - horizontalInsets).coerceAtLeast(1)
                            val previousContentHeight =
                                (previousSize.height - verticalInsets).coerceAtLeast(1)
                            val widthDelta =
                                kotlin.math.abs(
                                    currentContentWidth - previousContentWidth,
                                )
                            val heightDelta =
                                kotlin.math.abs(
                                    currentContentHeight - previousContentHeight,
                                )
                            val targetSize =
                                if (heightDelta > widthDelta) {
                                    val contentHeight = currentContentHeight
                                    Dimension(
                                        (contentHeight * GAME_ASPECT_RATIO).toInt() +
                                            horizontalInsets,
                                        contentHeight + verticalInsets,
                                    )
                                } else {
                                    val contentWidth = currentContentWidth
                                    Dimension(
                                        contentWidth + horizontalInsets,
                                        (contentWidth / GAME_ASPECT_RATIO).toInt() +
                                            verticalInsets,
                                    )
                                }
                            val constrainedSize =
                                Dimension(
                                    targetSize.width
                                        .coerceAtLeast(MINIMUM_WINDOW_WIDTH),
                                    targetSize.height
                                        .coerceAtLeast(MINIMUM_WINDOW_HEIGHT),
                                )
                            previousSize = constrainedSize

                            if (currentSize != constrainedSize) {
                                correctingSize = true
                                try {
                                    window.size = constrainedSize
                                } finally {
                                    correctingSize = false
                                }
                            }
                        }
                    },
                )
            }
        },
    )
}
