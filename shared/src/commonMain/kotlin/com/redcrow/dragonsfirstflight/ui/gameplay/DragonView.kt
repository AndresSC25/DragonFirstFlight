package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.sprite
import korlibs.time.milliseconds

private const val DRAGON_FRAME_WIDTH: Int = 32
private const val DRAGON_FRAME_HEIGHT: Int = 32
private const val DRAGON_FRAME_COLUMNS: Int = 6
private const val DRAGON_FRAME_ROWS: Int = 1
private const val DRAGON_FRAME_DURATION_MS: Int = 120

internal class DragonView private constructor(
    private val sprite: Sprite,
    private val flightAnimation: SpriteAnimation
) {
    private var isAnimationStopped: Boolean = false

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean
    ) {
        sprite.x = snapshot.dragonX
        sprite.y = snapshot.dragonY
        sprite.alpha = if (
            snapshot.isImpactFeedbackActive ||
            snapshot.isInvulnerabilityBlinkDim
        ) 0.45 else 1.0

        val shouldAnimate =
            snapshot.status == GameStatus.RUNNING &&
                !isPaused

        if (shouldAnimate && isAnimationStopped) {
            sprite.playAnimationLooped(
                spriteAnimation = flightAnimation,
                spriteDisplayTime =
                    DRAGON_FRAME_DURATION_MS.milliseconds
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
            spriteMap: Bitmap
        ): DragonView {
            require(
                spriteMap.width ==
                    DRAGON_FRAME_WIDTH *
                    DRAGON_FRAME_COLUMNS
            ) {
                "Invalid dragon spritesheet width."
            }

            require(
                spriteMap.height == DRAGON_FRAME_HEIGHT
            ) {
                "Invalid dragon spritesheet height."
            }

            val flightAnimation = SpriteAnimation(
                spriteMap = spriteMap,
                spriteWidth = DRAGON_FRAME_WIDTH,
                spriteHeight = DRAGON_FRAME_HEIGHT,
                marginTop = 0,
                marginLeft = 0,
                columns = DRAGON_FRAME_COLUMNS,
                rows = DRAGON_FRAME_ROWS,
                offsetBetweenColumns = 0,
                offsetBetweenRows = 0
            )

            val sprite = container.sprite(
                initialAnimation = flightAnimation
            )

            val dragonScale =
                GameEngineConfig.DRAGON_SIZE /
                    DRAGON_FRAME_WIDTH.toDouble()

            sprite.scaleX = dragonScale
            sprite.scaleY = dragonScale
            sprite.smoothing = false

            sprite.playAnimationLooped(
                spriteAnimation = flightAnimation,
                spriteDisplayTime =
                    DRAGON_FRAME_DURATION_MS.milliseconds
            )

            return DragonView(
                sprite = sprite,
                flightAnimation = flightAnimation
            )
        }
    }
}
