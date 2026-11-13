package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

internal const val CATAPULT_PROJECTILE_PATH = "sprites/boss/catapult/catapult_projectile.png"
private const val FRAME_SIZE = 128
private const val FRAME_COUNT = 8
private const val FRAME_SECONDS = 0.08

internal class ProjectileView private constructor(
    private val container: Container,
    private val frames: List<BmpSlice>,
) {
    private val pool = mutableListOf<Image>()

    fun update(snapshot: GameSnapshot) {
        val projectiles = snapshot.projectiles
        val count = if (snapshot.isRunning && !snapshot.isZoneTransitioning) projectiles.size else 0
        while (pool.size < count) {
            pool += container.image(frames[0]) { smoothing = false }
        }
        for (index in 0 until count) {
            val projectile = projectiles[index]
            val image = pool[index]
            image.bitmap = frames[(projectile.ageSeconds / FRAME_SECONDS).toInt() % FRAME_COUNT]
            image.x = projectile.x
            image.y = projectile.y
            image.scaleX = projectile.size / FRAME_SIZE
            image.scaleY = projectile.size / FRAME_SIZE
            image.visible = true
        }
        for (index in count until pool.size) pool[index].visible = false
    }

    companion object {
        fun create(
            container: Container,
            spriteMap: Bitmap,
        ): ProjectileView {
            require(spriteMap.width == FRAME_SIZE * FRAME_COUNT && spriteMap.height == FRAME_SIZE) {
                "Catapult projectile spritesheet must be 1024x128, but was ${spriteMap.width}x${spriteMap.height}."
            }
            val frames =
                List(FRAME_COUNT) { column ->
                    spriteMap.sliceWithSize(column * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE)
                }
            return ProjectileView(container, frames)
        }
    }
}
