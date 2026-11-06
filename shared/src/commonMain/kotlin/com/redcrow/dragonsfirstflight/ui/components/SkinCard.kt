package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.skins.DragonSkin
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.solidRect
import korlibs.korge.view.sprite
import korlibs.korge.view.text
import korlibs.time.milliseconds

internal fun Container.skinCard(
    skin: DragonSkin,
    animation: SpriteAnimation,
    width: Double,
    height: Double,
): Sprite {
    solidRect(
        width = width,
        height = height,
        color = Colors["#101827"],
    ) {
        alpha = 0.6
    }
    val preview =
        sprite(initialAnimation = animation) {
            x = (width - 96.0) / 2.0
            y = 12.0
            scaleX = 3.0
            scaleY = 3.0
            smoothing = false
        }
    preview.playAnimationLooped(
        spriteAnimation = animation,
        spriteDisplayTime = 140.milliseconds,
    )

    val name =
        text(
            text = skin.displayName,
            textSize = 24.0,
            color = Colors.WHITE,
        )
    name.x = (width - name.width) / 2.0
    name.y = 120.0
    return preview
}
