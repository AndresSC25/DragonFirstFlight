package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.image

private const val SHIELD_POWER_UP_FRAME_SIZE: Int = 32
private const val SHIELD_POWER_UP_FRAME_COUNT: Int = 8

internal class ShieldPowerUpView private constructor(
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
        val visibleCount = if (isRunning) snapshot.shieldPowerUps.size else 0
        while (pool.size < visibleCount) {
            pool +=
                AnimatedSpriteView.create(
                    container = container,
                    bitmap = spriteMap,
                    config =
                        AnimatedSpriteConfig(
                            frameWidth = SHIELD_POWER_UP_FRAME_SIZE,
                            frameHeight = SHIELD_POWER_UP_FRAME_SIZE,
                            columns = SHIELD_POWER_UP_FRAME_COUNT,
                            rows = 1,
                            frameDurationMs = 120,
                        ),
                )
        }
        snapshot.shieldPowerUps.forEachIndexed { index, powerUp ->
            val sprite = pool[index].view
            sprite.x = powerUp.worldX - snapshot.shieldPowerUpScrollX
            sprite.y = powerUp.y
            sprite.scaleX = GameEngineConfig.SHIELD_POWER_UP_SIZE / SHIELD_POWER_UP_FRAME_SIZE
            sprite.scaleY = GameEngineConfig.SHIELD_POWER_UP_SIZE / SHIELD_POWER_UP_FRAME_SIZE
            sprite.alpha = horizontalEdgeAlpha(views, sprite.x, GameEngineConfig.SHIELD_POWER_UP_SIZE)
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
        ): ShieldPowerUpView = ShieldPowerUpView(container, bitmap, views)
    }
}

internal class GreenShieldView private constructor(
    private val container: Container,
    private val spriteMap: Bitmap,
) {
    private var frame = 0
    private var frameElapsedSeconds = 0.0
    private val view =
        container.image(spriteMap) {
            visible = false
        }

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
        elapsedSeconds: Double,
    ) {
        val visible = snapshot.isShieldActive && snapshot.status == GameStatus.RUNNING
        view.visible = visible
        if (!visible) return
        view.alpha = 0.45

        if (!isPaused) {
            frameElapsedSeconds += elapsedSeconds
            if (frameElapsedSeconds >= 0.12) {
                frame = (frame + 1) % SHIELD_POWER_UP_FRAME_COUNT
                frameElapsedSeconds %= 0.12
            }
        }

        view.bitmap =
            spriteMap.sliceWithSize(
                frame * SHIELD_POWER_UP_FRAME_SIZE,
                0,
                SHIELD_POWER_UP_FRAME_SIZE,
                SHIELD_POWER_UP_FRAME_SIZE,
            )
        view.x =
            snapshot.dragonX +
            (snapshot.dragonSize - GameEngineConfig.GREEN_SHIELD_SIZE) / 2.0
        view.y =
            snapshot.dragonY +
            (snapshot.dragonSize - GameEngineConfig.GREEN_SHIELD_SIZE) / 2.0
        view.scaleX = GameEngineConfig.GREEN_SHIELD_SIZE / SHIELD_POWER_UP_FRAME_SIZE
        view.scaleY = GameEngineConfig.GREEN_SHIELD_SIZE / SHIELD_POWER_UP_FRAME_SIZE
        view.smoothing = false
    }

    companion object {
        fun create(
            container: Container,
            bitmap: Bitmap,
        ): GreenShieldView {
            require(bitmap.width == 256 && bitmap.height == 32) {
                "Invalid green shield spritesheet dimensions."
            }
            return GreenShieldView(container, bitmap)
        }
    }
}
