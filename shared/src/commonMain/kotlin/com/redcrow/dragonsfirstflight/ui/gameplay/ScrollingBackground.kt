package com.redcrow.dragonsfirstflight.ui.gameplay

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

private const val DEFAULT_SCROLL_SPEED: Double = 70.0

internal class ScrollingBackground private constructor(
    private val firstBackground: Image,
    private val secondBackground: Image,
    private val backgroundWidth: Double,
    private val scrollSpeed: Double
) {
    fun update(elapsedSeconds: Double) {
        require(elapsedSeconds >= 0.0) {
            "Elapsed time must not be negative."
        }

        val movement =
            scrollSpeed * elapsedSeconds

        firstBackground.x -= movement
        secondBackground.x -= movement

        if (firstBackground.x <= -backgroundWidth) {
            firstBackground.x =
                secondBackground.x + backgroundWidth
        }

        if (secondBackground.x <= -backgroundWidth) {
            secondBackground.x =
                firstBackground.x + backgroundWidth
        }
    }

    companion object {
        fun create(
            container: Container,
            bitmap: Bitmap,
            width: Double,
            height: Double,
            scrollSpeed: Double = DEFAULT_SCROLL_SPEED
        ): ScrollingBackground {
            require(bitmap.width > 0) {
                "Background bitmap width must be greater than zero."
            }

            require(bitmap.height > 0) {
                "Background bitmap height must be greater than zero."
            }

            require(width > 0.0) {
                "Background width must be greater than zero."
            }

            require(height > 0.0) {
                "Background height must be greater than zero."
            }

            require(scrollSpeed >= 0.0) {
                "Background scroll speed must not be negative."
            }

            val backgroundScaleX =
                width / bitmap.width.toDouble()

            val backgroundScaleY =
                height / bitmap.height.toDouble()

            val firstBackground =
                container.image(texture = bitmap) {
                    x = 0.0
                    y = 0.0
                    scaleX = backgroundScaleX
                    scaleY = backgroundScaleY
                    smoothing = false
                }

            val secondBackground =
                container.image(texture = bitmap) {
                    x = width
                    y = 0.0
                    scaleX = backgroundScaleX
                    scaleY = backgroundScaleY
                    smoothing = false
                }

            return ScrollingBackground(
                firstBackground = firstBackground,
                secondBackground = secondBackground,
                backgroundWidth = width,
                scrollSpeed = scrollSpeed
            )
        }
    }
}
