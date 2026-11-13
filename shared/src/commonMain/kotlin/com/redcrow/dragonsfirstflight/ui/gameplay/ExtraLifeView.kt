package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Sprite
import korlibs.korge.view.Views

private const val EXTRA_LIFE_FRAME_SIZE: Int = 384
private const val EXTRA_LIFE_FRAME_COUNT: Int = 8
private const val EXTRA_LIFE_FRAME_DURATION_MS: Int = 120

internal class ExtraLifeView private constructor(
    private val container: Container,
    private val spriteMap: Bitmap,
    private val views: Views,
) {
    private val pool = ArrayList<AnimatedSpriteView>()

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
    ) {
        val isRunning = snapshot.status == GameStatus.RUNNING
        val visibleCount = if (isRunning) snapshot.extraLives.size else 0
        while (pool.size < visibleCount) {
            pool +=
                AnimatedSpriteView.create(
                    container = container,
                    bitmap = spriteMap,
                    config =
                        AnimatedSpriteConfig(
                            frameWidth = EXTRA_LIFE_FRAME_SIZE,
                            frameHeight = EXTRA_LIFE_FRAME_SIZE,
                            columns = EXTRA_LIFE_FRAME_COUNT,
                            rows = 1,
                            frameDurationMs = EXTRA_LIFE_FRAME_DURATION_MS,
                        ),
                )
        }

        snapshot.extraLives.forEachIndexed { index, extraLife ->
            val sprite = pool[index]
            val view = sprite.view
            view.x = extraLife.worldX - snapshot.extraLifeScrollX
            view.y = extraLife.y
            view.scaleX = GameEngineConfig.EXTRA_LIFE_SIZE / EXTRA_LIFE_FRAME_SIZE
            view.scaleY = GameEngineConfig.EXTRA_LIFE_SIZE / EXTRA_LIFE_FRAME_SIZE
            view.alpha = horizontalEdgeAlpha(views, view.x, GameEngineConfig.EXTRA_LIFE_SIZE)
            view.visible = true
            sprite.update(shouldAnimate = !isPaused && isRunning)
        }
        for (index in visibleCount until pool.size) {
            val view: Sprite = pool[index].view
            view.visible = false
            pool[index].update(shouldAnimate = false)
        }
    }

    companion object {
        fun create(container: Container, bitmap: Bitmap, views: Views): ExtraLifeView {
            require(
                bitmap.width == EXTRA_LIFE_FRAME_SIZE * EXTRA_LIFE_FRAME_COUNT &&
                    bitmap.height == EXTRA_LIFE_FRAME_SIZE,
            ) {
                "Extra life sprite must be 3072x384, but was ${bitmap.width}x${bitmap.height}."
            }
            return ExtraLifeView(container, bitmap, views)
        }
    }
}
