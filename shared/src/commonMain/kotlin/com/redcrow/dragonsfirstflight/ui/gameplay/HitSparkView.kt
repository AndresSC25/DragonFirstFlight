package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.sprite
import korlibs.time.milliseconds

private const val HIT_SPARK_FRAME_SIZE: Int = 64
private const val HIT_SPARK_FRAME_COLUMNS: Int = 4
private const val HIT_SPARK_FRAME_ROWS: Int = 1
private const val HIT_SPARK_FRAME_DURATION_MS: Int = 80

internal class HitSparkView private constructor(
    private val sprite: Sprite,
    private val animation: SpriteAnimation,
) {
    private var wasImpactActive: Boolean = false
    private var isAnimationStopped: Boolean = true

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
    ) {
        if (!snapshot.isImpactFeedbackActive) {
            sprite.visible = false
            wasImpactActive = false
            return
        }

        val impactObstacle =
            snapshot.obstacles.firstOrNull {
                it.id == snapshot.impactObstacleId
            }
        if (impactObstacle == null) {
            sprite.visible = false
            return
        }

        sprite.x = impactObstacle.x + impactObstacle.width / 2.0 -
            HIT_SPARK_FRAME_SIZE / 2.0
        sprite.y = impactObstacle.y + impactObstacle.height / 2.0 -
            HIT_SPARK_FRAME_SIZE / 2.0
        sprite.visible = true

        if (isPaused) {
            if (!isAnimationStopped) {
                sprite.stopAnimation()
                isAnimationStopped = true
            }
            return
        }

        if (!wasImpactActive || isAnimationStopped) {
            sprite.playAnimationLooped(
                spriteAnimation = animation,
                spriteDisplayTime =
                    HIT_SPARK_FRAME_DURATION_MS.milliseconds,
            )
            isAnimationStopped = false
        }
        wasImpactActive = true
    }

    companion object {
        fun create(
            container: Container,
            spriteMap: Bitmap,
        ): HitSparkView {
            require(
                spriteMap.width ==
                    HIT_SPARK_FRAME_SIZE * HIT_SPARK_FRAME_COLUMNS,
            ) {
                "Invalid hit spark spritesheet width."
            }
            require(spriteMap.height == HIT_SPARK_FRAME_SIZE) {
                "Invalid hit spark spritesheet height."
            }

            val animation =
                SpriteAnimation(
                    spriteMap = spriteMap,
                    spriteWidth = HIT_SPARK_FRAME_SIZE,
                    spriteHeight = HIT_SPARK_FRAME_SIZE,
                    marginTop = 0,
                    marginLeft = 0,
                    columns = HIT_SPARK_FRAME_COLUMNS,
                    rows = HIT_SPARK_FRAME_ROWS,
                    offsetBetweenColumns = 0,
                    offsetBetweenRows = 0,
                )
            val sprite =
                container.sprite(initialAnimation = animation) {
                    visible = false
                    smoothing = false
                }

            return HitSparkView(sprite, animation)
        }
    }
}
