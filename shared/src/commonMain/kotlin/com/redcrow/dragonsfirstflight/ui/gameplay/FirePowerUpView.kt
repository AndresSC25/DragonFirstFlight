package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.Views
import korlibs.korge.view.image
import korlibs.image.bitmap.sliceWithSize

private const val FIRE_POWER_UP_FRAME_SIZE: Int = 384
private const val FIRE_POWER_UP_FRAME_COUNT: Int = 6

internal class FirePowerUpView private constructor(
    private val container: Container,
    private val spriteMap: Bitmap,
    private val views: Views,
) {
    private val pool = ArrayList<AnimatedSpriteView>()

    fun update(snapshot: GameSnapshot, isPaused: Boolean) {
        val isRunning = snapshot.status == GameStatus.RUNNING
        val visibleCount = if (isRunning) snapshot.firePowerUps.size else 0
        while (pool.size < visibleCount) {
            pool +=
                AnimatedSpriteView.create(
                    container = container,
                    bitmap = spriteMap,
                    config =
                        AnimatedSpriteConfig(
                            frameWidth = FIRE_POWER_UP_FRAME_SIZE,
                            frameHeight = FIRE_POWER_UP_FRAME_SIZE,
                            columns = FIRE_POWER_UP_FRAME_COUNT,
                            rows = 1,
                            frameDurationMs = 120,
                        ),
                )
        }
        snapshot.firePowerUps.forEachIndexed { index, powerUp ->
            val sprite = pool[index].view
            sprite.x = powerUp.worldX - snapshot.firePowerUpScrollX
            sprite.y = powerUp.y
            sprite.scaleX = GameEngineConfig.FIRE_POWER_UP_SIZE / FIRE_POWER_UP_FRAME_SIZE
            sprite.scaleY = GameEngineConfig.FIRE_POWER_UP_SIZE / FIRE_POWER_UP_FRAME_SIZE
            sprite.alpha = horizontalEdgeAlpha(views, sprite.x, GameEngineConfig.FIRE_POWER_UP_SIZE)
            sprite.visible = true
            pool[index].update(shouldAnimate = !isPaused && isRunning)
        }
        for (index in visibleCount until pool.size) {
            pool[index].view.visible = false
            pool[index].update(shouldAnimate = false)
        }
    }

    companion object {
        fun create(container: Container, bitmap: Bitmap, views: Views): FirePowerUpView =
            FirePowerUpView(container, bitmap, views)
    }
}

internal class FireBallView private constructor(
    private val container: Container,
    private val frames: List<korlibs.image.bitmap.BmpSlice>,
) {
    private val pool = mutableListOf<korlibs.korge.view.Image>()

    fun update(snapshot: GameSnapshot, isPaused: Boolean) {
        val visibleCount =
            if (snapshot.status == GameStatus.RUNNING && !isPaused) {
                snapshot.fireBalls.size
            } else {
                0
            }
        while (pool.size < visibleCount) {
            pool += container.image(frames[0]) { smoothing = false }
        }
        snapshot.fireBalls.forEachIndexed { index, ball ->
            if (index >= pool.size) return@forEachIndexed
            val frame =
                (ball.ageSeconds / 0.08).toInt().coerceAtLeast(0) %
                    frames.size
            val image = pool[index]
            image.bitmap = frames[frame]
            image.x = ball.x
            image.y = ball.y
            image.scaleX = ball.size / 32.0
            image.scaleY = ball.size / 32.0
            image.visible = true
        }
        for (index in visibleCount until pool.size) {
            pool[index].visible = false
        }
    }

    companion object {
        fun create(container: Container, bitmap: Bitmap): FireBallView {
            require(bitmap.width == 192 && bitmap.height == 32) {
                "Fire ball sprite must be 192x32, but was ${bitmap.width}x${bitmap.height}."
            }
            val frames =
                List(6) { column ->
                    bitmap.sliceWithSize(column * 32, 0, 32, 32)
                }
            return FireBallView(container, frames)
        }
    }
}

internal class DragonFlamesView private constructor(
    private val animatedSprite: AnimatedSpriteView,
) {
    fun update(snapshot: GameSnapshot, isPaused: Boolean) {
        val visible = snapshot.isFirePowerActive && snapshot.status == GameStatus.RUNNING
        val sprite = animatedSprite.view
        sprite.visible = visible
        if (!visible) return
        sprite.x =
            snapshot.dragonX +
                snapshot.dragonSize -
                12.0
        sprite.y =
            snapshot.dragonY +
                (snapshot.dragonSize - GameEngineConfig.FIRE_FLAME_SIZE) / 2.0
        animatedSprite.update(
            shouldAnimate = !isPaused && snapshot.status == GameStatus.RUNNING,
        )
    }

    companion object {
        fun create(container: Container, bitmap: Bitmap): DragonFlamesView {
            val frameSize = 362
            require(bitmap.width == frameSize * 6 && bitmap.height == frameSize * 2) {
                "Invalid flames_dragon sprite dimensions: ${bitmap.width}x${bitmap.height}"
            }
            val animatedSprite =
                AnimatedSpriteView.create(
                    container = container,
                    bitmap = bitmap,
                    config =
                        AnimatedSpriteConfig(
                            frameWidth = frameSize,
                            frameHeight = frameSize,
                            columns = 6,
                            rows = 2,
                            frameDurationMs = 100,
                        ),
                )
            val sprite = animatedSprite.view
            sprite.scaleX = GameEngineConfig.FIRE_FLAME_SIZE / frameSize
            sprite.scaleY = GameEngineConfig.FIRE_FLAME_SIZE / frameSize
            sprite.visible = false
            return DragonFlamesView(animatedSprite)
        }
    }
}
