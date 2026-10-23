package com.redcrow.dragonsfirstflight.ui.layout

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.image
import kotlin.math.max

internal fun Container.createResponsiveBackdrop(
    bitmap: Bitmap,
    views: Views,
): Image {
    require(bitmap.width > 0) {
        "Responsive backdrop width must be greater than zero."
    }

    require(bitmap.height > 0) {
        "Responsive backdrop height must be greater than zero."
    }

    val backdrop =
        image(texture = bitmap) {
            smoothing = false
        }

    var previousLeft = Double.NaN
    var previousTop = Double.NaN
    var previousWidth = Double.NaN
    var previousHeight = Double.NaN

    fun updateLayout() {
        val virtualLeft =
            views.actualVirtualLeft.toDouble()

        val virtualTop =
            views.actualVirtualTop.toDouble()

        val virtualWidth =
            views.actualVirtualWidth.toDouble()

        val virtualHeight =
            views.actualVirtualHeight.toDouble()

        if (
            virtualLeft == previousLeft &&
            virtualTop == previousTop &&
            virtualWidth == previousWidth &&
            virtualHeight == previousHeight
        ) {
            return
        }

        previousLeft = virtualLeft
        previousTop = virtualTop
        previousWidth = virtualWidth
        previousHeight = virtualHeight

        val horizontalScale =
            virtualWidth / bitmap.width.toDouble()

        val verticalScale =
            virtualHeight / bitmap.height.toDouble()

        val backdropScale =
            max(horizontalScale, verticalScale)

        val scaledWidth =
            bitmap.width * backdropScale

        val scaledHeight =
            bitmap.height * backdropScale

        backdrop.scaleX = backdropScale
        backdrop.scaleY = backdropScale

        backdrop.x =
            virtualLeft +
            (virtualWidth - scaledWidth) / 2.0

        backdrop.y =
            virtualTop +
            (virtualHeight - scaledHeight) / 2.0
    }

    updateLayout()

    backdrop.addUpdater {
        updateLayout()
    }

    return backdrop
}
