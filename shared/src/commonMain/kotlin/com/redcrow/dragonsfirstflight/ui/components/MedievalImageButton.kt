package com.redcrow.dragonsfirstflight.ui.components

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image
import korlibs.korge.view.text

internal fun Container.createMedievalImageButton(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    texture: Bitmap,
    label: String,
    textSize: Double = 24.0,
    textVerticalOffset: Double = 3.0,
    onClick: () -> Unit,
): Image {
    require(width > 0.0) {
        "Button width must be greater than zero."
    }

    require(height > 0.0) {
        "Button height must be greater than zero."
    }

    require(textSize > 0.0) {
        "Button text size must be greater than zero."
    }

    val button =
        image(texture = texture) {
            this.x = x
            this.y = y
            scaleX = width / texture.width.toDouble()
            scaleY = height / texture.height.toDouble()
            smoothing = false
        }

    val buttonText =
        text(
            text = label,
            textSize = textSize,
            color = Colors.WHITE,
        )

    buttonText.x =
        x + (width - buttonText.width) / 2.0

    buttonText.y =
        y +
        (height - buttonText.height) / 2.0 +
        textVerticalOffset

    button.onClick {
        onClick()
    }

    buttonText.onClick {
        onClick()
    }

    return button
}
