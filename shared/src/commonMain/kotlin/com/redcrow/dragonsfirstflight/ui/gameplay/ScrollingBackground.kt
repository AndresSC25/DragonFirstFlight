package com.redcrow.dragonsfirstflight.ui.gameplay

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Views
import korlibs.korge.view.image

private const val DEFAULT_SCROLL_SPEED: Double = 70.0

internal class ScrollingBackground private constructor(
    private val firstBackground: Image,
    private val secondBackground: Image,
    private val bitmap: Bitmap,
    private val views: Views,
    private val scrollSpeed: Double,
) {
    private var backgroundWidth: Double = 0.0

    private var previousLeft: Double = Double.NaN
    private var previousTop: Double = Double.NaN
    private var previousWidth: Double = Double.NaN
    private var previousHeight: Double = Double.NaN

    fun update(elapsedSeconds: Double) {
        require(elapsedSeconds >= 0.0) {
            "Elapsed time must not be negative."
        }

        updateLayoutIfRequired()

        if (backgroundWidth <= 0.0) {
            return
        }

        val movement =
            scrollSpeed * elapsedSeconds

        firstBackground.x -= movement
        secondBackground.x -= movement

        val leftBoundary =
            views.actualVirtualLeft.toDouble()

        if (
            firstBackground.x + backgroundWidth <=
            leftBoundary
        ) {
            firstBackground.x =
                secondBackground.x + backgroundWidth
        }

        if (
            secondBackground.x + backgroundWidth <=
            leftBoundary
        ) {
            secondBackground.x =
                firstBackground.x + backgroundWidth
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

        backgroundWidth = currentWidth

        val backgroundScaleX =
            currentWidth / bitmap.width.toDouble()

        val backgroundScaleY =
            currentHeight / bitmap.height.toDouble()

        firstBackground.x = currentLeft
        firstBackground.y = currentTop
        firstBackground.scaleX = backgroundScaleX
        firstBackground.scaleY = backgroundScaleY

        secondBackground.x =
            currentLeft + backgroundWidth

        secondBackground.y = currentTop
        secondBackground.scaleX = backgroundScaleX
        secondBackground.scaleY = backgroundScaleY
    }

    companion object {
        fun create(
            container: Container,
            bitmap: Bitmap,
            views: Views,
            scrollSpeed: Double = DEFAULT_SCROLL_SPEED,
        ): ScrollingBackground {
            require(bitmap.width > 0) {
                "Background bitmap width must be greater than zero."
            }

            require(bitmap.height > 0) {
                "Background bitmap height must be greater than zero."
            }

            require(scrollSpeed >= 0.0) {
                "Background scroll speed must not be negative."
            }

            val firstBackground =
                container.image(texture = bitmap) {
                    smoothing = false
                }

            val secondBackground =
                container.image(texture = bitmap) {
                    smoothing = false
                }

            return ScrollingBackground(
                firstBackground = firstBackground,
                secondBackground = secondBackground,
                bitmap = bitmap,
                views = views,
                scrollSpeed = scrollSpeed,
            ).also {
                it.update(0.0)
            }
        }
    }
}
