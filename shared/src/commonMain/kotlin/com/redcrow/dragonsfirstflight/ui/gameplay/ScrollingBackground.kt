package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Views
import korlibs.korge.view.container
import korlibs.korge.view.image
import kotlin.math.ceil

internal class ScrollingBackground private constructor(
    private val layers: List<ScrollingLayer>,
) {
    fun update(elapsedSeconds: Double) {
        require(elapsedSeconds >= 0.0) {
            "Elapsed time must not be negative."
        }

        layers.forEach { layer ->
            layer.update(elapsedSeconds)
        }
    }

    class Layer(
        val bitmap: Bitmap,
        val speedFactor: Double,
        val mirrorTiles: Boolean = false,
        val heightFraction: Double = 1.0,
        val offsetYFraction: Double = 0.0,
    ) {
        init {
            require(bitmap.width > 0) {
                "Background layer width must be greater than zero."
            }

            require(bitmap.height > 0) {
                "Background layer height must be greater than zero."
            }

            require(speedFactor >= 0.0) {
                "Background layer speed factor must not be negative."
            }

            require(heightFraction > 0.0 && heightFraction <= 1.0) {
                "Background layer height fraction must be in (0, 1]."
            }

            require(offsetYFraction >= 0.0 && offsetYFraction < 1.0) {
                "Background layer vertical offset must be in [0, 1)."
            }
        }
    }

    companion object {
        fun create(
            container: Container,
            layers: List<Layer>,
            views: Views,
        ): ScrollingBackground {
            val scrollingLayers =
                layers.map { layer ->
                    ScrollingLayer(
                        layerContainer = container.container(),
                        layer = layer,
                        views = views,
                        speed = GameEngineConfig.OBSTACLE_SPEED * layer.speedFactor,
                    )
                }

            return ScrollingBackground(
                layers = scrollingLayers,
            ).also {
                it.update(0.0)
            }
        }
    }
}

internal fun tilesToCover(
    viewportWidth: Double,
    tileWidth: Double,
    tilesPerPeriod: Int,
): Int = ceil(viewportWidth / tileWidth).toInt() + tilesPerPeriod

private class ScrollingLayer(
    private val layerContainer: Container,
    private val layer: ScrollingBackground.Layer,
    private val views: Views,
    private val speed: Double,
) {
    private val tiles =
        mutableListOf<Image>()

    private val tilesPerPeriod: Int =
        if (layer.mirrorTiles) 2 else 1

    private val periodBitmapWidth: Double =
        layer.bitmap.width.toDouble() * tilesPerPeriod

    private var scrolledBitmapPixels: Double = 0.0

    private var scale: Double = 1.0

    private var previousLeft: Double = Double.NaN
    private var previousTop: Double = Double.NaN
    private var previousWidth: Double = Double.NaN
    private var previousHeight: Double = Double.NaN

    fun update(elapsedSeconds: Double) {
        updateLayoutIfRequired()

        scrolledBitmapPixels =
            (scrolledBitmapPixels + speed * elapsedSeconds / scale) %
            periodBitmapWidth

        val tileWidth =
            layer.bitmap.width * scale

        val firstTileX =
            previousLeft - scrolledBitmapPixels * scale

        tiles.forEachIndexed { index, tile ->
            val tileX =
                firstTileX + index * tileWidth

            val isFlipped =
                tile.scaleX < 0.0

            tile.x =
                if (isFlipped) {
                    tileX + tileWidth
                } else {
                    tileX
                }
        }
    }

    private fun updateLayoutIfRequired() {
        val currentLeft =
            views.actualVirtualLeft.toDouble()

        val currentTop =
            views.actualVirtualTop.toDouble()

        val currentWidth =
            views.actualVirtualWidth.toDouble()

        val currentHeight =
            views.actualVirtualHeight.toDouble()

        require(currentWidth > 0.0) {
            "Actual virtual width must be greater than zero."
        }

        require(currentHeight > 0.0) {
            "Actual virtual height must be greater than zero."
        }

        val layoutHasNotChanged =
            currentLeft == previousLeft &&
                currentTop == previousTop &&
                currentWidth == previousWidth &&
                currentHeight == previousHeight

        if (layoutHasNotChanged) {
            return
        }

        previousLeft = currentLeft
        previousTop = currentTop
        previousWidth = currentWidth
        previousHeight = currentHeight

        val layerHeight =
            currentHeight * layer.heightFraction

        val layerTop =
            currentTop + currentHeight - layerHeight +
                currentHeight * layer.offsetYFraction

        scale = layerHeight / layer.bitmap.height

        val requiredTiles =
            tilesToCover(
                viewportWidth = currentWidth,
                tileWidth = layer.bitmap.width * scale,
                tilesPerPeriod = tilesPerPeriod,
            )

        while (tiles.size < requiredTiles) {
            tiles +=
                layerContainer.image(texture = layer.bitmap) {
                    smoothing = false
                }
        }

        while (tiles.size > requiredTiles) {
            tiles.removeAt(tiles.lastIndex).removeFromParent()
        }

        tiles.forEachIndexed { index, tile ->
            val isMirrored =
                layer.mirrorTiles && index % 2 == 1

            tile.scaleX = if (isMirrored) -scale else scale
            tile.scaleY = scale
            tile.y = layerTop
        }
    }
}
