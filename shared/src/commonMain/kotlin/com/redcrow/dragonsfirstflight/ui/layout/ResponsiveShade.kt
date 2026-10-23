package com.redcrow.dragonsfirstflight.ui.layout

import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.solidRect

internal fun Container.createResponsiveShade(
    views: Views,
    color: RGBA,
    shadeAlpha: Double,
): SolidRect {
    require(shadeAlpha in 0.0..1.0) {
        "Shade alpha must be between zero and one."
    }

    val shade =
        solidRect(
            width = 1.0,
            height = 1.0,
            color = color,
        ) {
            alpha = shadeAlpha
        }

    var previousLeft = Double.NaN
    var previousTop = Double.NaN
    var previousWidth = Double.NaN
    var previousHeight = Double.NaN

    fun updateLayout() {
        val left =
            views.actualVirtualLeft.toDouble()

        val top =
            views.actualVirtualTop.toDouble()

        val width =
            views.actualVirtualWidth.toDouble()

        val height =
            views.actualVirtualHeight.toDouble()

        if (
            left == previousLeft &&
            top == previousTop &&
            width == previousWidth &&
            height == previousHeight
        ) {
            return
        }

        previousLeft = left
        previousTop = top
        previousWidth = width
        previousHeight = height

        shade.x = left
        shade.y = top
        shade.scaleX = width
        shade.scaleY = height
    }

    updateLayout()

    shade.addUpdater {
        updateLayout()
    }

    return shade
}
