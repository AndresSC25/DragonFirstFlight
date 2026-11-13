package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.CoinValue
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Views
import korlibs.korge.view.image

private const val COIN_SIZE: Double = GameEngineConfig.COIN_SIZE
private const val COIN_CELL_SIZE: Int = 64
private const val COIN_FRAME_COUNT: Int = 12
private const val COIN_ROW_COUNT: Int = 2
private const val COIN_FRAME_SECONDS: Double = 0.08

internal class CoinView private constructor(
    private val container: Container,
    private val frames: Array<Array<BmpSlice>>,
    private val views: Views,
) {
    private val pool = ArrayList<Image>()
    private var animationSeconds: Double = 0.0

    fun update(
        snapshot: GameSnapshot,
        isPaused: Boolean,
        elapsedSeconds: Double,
    ) {
        val isRunning = snapshot.status == GameStatus.RUNNING
        if (isRunning && !isPaused) {
            animationSeconds += elapsedSeconds
        }

        val coins = snapshot.coins
        val visibleCount = if (isRunning) coins.size else 0
        while (pool.size < visibleCount) {
            pool +=
                container.image(frames[0][0]) {
                    smoothing = true
                    scaleX = COIN_SIZE / COIN_CELL_SIZE.toDouble()
                    scaleY = COIN_SIZE / COIN_CELL_SIZE.toDouble()
                }
        }

        val frameIndex = (animationSeconds / COIN_FRAME_SECONDS).toInt() % COIN_FRAME_COUNT
        for (index in 0 until visibleCount) {
            val coin = coins[index]
            val image = pool[index]
            val x = coin.worldX - snapshot.coinScrollX
            val row = if (coin.value == CoinValue.FIVE_HUNDRED) 1 else 0
            image.bitmap = frames[row][frameIndex]
            image.x = x
            image.y = coin.y
            image.alpha = horizontalEdgeAlpha(views, x, COIN_SIZE)
            image.visible = true
        }
        for (index in visibleCount until pool.size) {
            pool[index].visible = false
        }
    }

    companion object {
        fun create(
            container: Container,
            atlas: Bitmap,
            views: Views,
        ): CoinView {
            require(
                atlas.width == COIN_CELL_SIZE * COIN_FRAME_COUNT &&
                    atlas.height == COIN_CELL_SIZE * COIN_ROW_COUNT,
            ) {
                "Coin atlas must be 768x128, but was ${atlas.width}x${atlas.height}."
            }
            val frames =
                Array<Array<BmpSlice>>(COIN_ROW_COUNT) { row ->
                    Array<BmpSlice>(COIN_FRAME_COUNT) { column ->
                        atlas.sliceWithSize(
                            x = column * COIN_CELL_SIZE,
                            y = row * COIN_CELL_SIZE,
                            width = COIN_CELL_SIZE,
                            height = COIN_CELL_SIZE,
                        )
                    }
                }
            return CoinView(container = container, frames = frames, views = views)
        }
    }
}
