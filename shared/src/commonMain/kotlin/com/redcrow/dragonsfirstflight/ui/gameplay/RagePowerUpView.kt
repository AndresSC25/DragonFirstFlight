package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Views

private const val RAGE_POWER_UP_FRAME_SIZE: Int = 32
private const val RAGE_POWER_UP_FRAME_COUNT: Int = 6

internal class RagePowerUpView private constructor(
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
        val visibleCount =
            if (isRunning) snapshot.ragePowerUps.size else 0
        while (pool.size < visibleCount) {
            pool +=
                AnimatedSpriteView.create(
                    container = container,
                    bitmap = spriteMap,
                    config =
                        AnimatedSpriteConfig(
                            frameWidth = RAGE_POWER_UP_FRAME_SIZE,
                            frameHeight = RAGE_POWER_UP_FRAME_SIZE,
                            columns = RAGE_POWER_UP_FRAME_COUNT,
                            rows = 1,
                            frameDurationMs = 120,
                        ),
                )
        }
        snapshot.ragePowerUps.forEachIndexed { index, powerUp ->
            val sprite = pool[index].view
            sprite.x = powerUp.worldX - snapshot.ragePowerUpScrollX
            sprite.y = powerUp.y
            sprite.scaleX =
                GameEngineConfig.RAGE_POWER_UP_SIZE / RAGE_POWER_UP_FRAME_SIZE
            sprite.scaleY =
                GameEngineConfig.RAGE_POWER_UP_SIZE / RAGE_POWER_UP_FRAME_SIZE
            sprite.alpha =
                horizontalEdgeAlpha(
                    views,
                    sprite.x,
                    GameEngineConfig.RAGE_POWER_UP_SIZE,
                )
            sprite.visible = true
            pool[index].update(shouldAnimate = !isPaused && isRunning)
        }
        for (index in visibleCount until pool.size) {
            pool[index].view.visible = false
            pool[index].update(shouldAnimate = false)
        }
    }

    companion object {
        fun create(
            container: Container,
            bitmap: Bitmap,
            views: Views,
        ): RagePowerUpView = RagePowerUpView(container, bitmap, views)
    }
}
