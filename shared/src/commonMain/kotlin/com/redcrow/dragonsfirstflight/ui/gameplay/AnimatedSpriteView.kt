package com.redcrow.dragonsfirstflight.ui.gameplay

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.sprite
import korlibs.time.milliseconds

internal data class AnimatedSpriteConfig(
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int,
    val rows: Int,
    val frameDurationMs: Int
) {
    init {
        require(frameWidth > 0 && frameHeight > 0) {
            "Animated sprite frame dimensions must be positive."
        }
        require(columns > 0 && rows > 0) {
            "Animated sprite rows and columns must be positive."
        }
        require(frameDurationMs > 0) {
            "Animated sprite frame duration must be positive."
        }
    }
}

internal class AnimatedSpriteView private constructor(
    private val sprite: Sprite,
    private val animation: SpriteAnimation,
    private val frameDurationMs: Int
) {
    private var isAnimationStopped = true

    val view: Sprite
        get() = sprite

    fun update(shouldAnimate: Boolean) {
        if (shouldAnimate && isAnimationStopped) {
            sprite.playAnimationLooped(
                spriteAnimation = animation,
                spriteDisplayTime = frameDurationMs.milliseconds
            )
            isAnimationStopped = false
        } else if (!shouldAnimate && !isAnimationStopped) {
            sprite.stopAnimation()
            isAnimationStopped = true
        }
    }

    companion object {
        fun create(
            container: Container,
            bitmap: Bitmap,
            config: AnimatedSpriteConfig
        ): AnimatedSpriteView {
            require(
                bitmap.width >= config.frameWidth * config.columns &&
                    bitmap.height >= config.frameHeight * config.rows
            ) {
                "Invalid animated sprite dimensions."
            }

            val animation = SpriteAnimation(
                spriteMap = bitmap,
                spriteWidth = config.frameWidth,
                spriteHeight = config.frameHeight,
                marginTop = 0,
                marginLeft = 0,
                columns = config.columns,
                rows = config.rows,
                offsetBetweenColumns = 0,
                offsetBetweenRows = 0
            )
            val sprite = container.sprite(initialAnimation = animation) {
                smoothing = false
            }
            return AnimatedSpriteView(
                sprite = sprite,
                animation = animation,
                frameDurationMs = config.frameDurationMs
            )
        }
    }
}
