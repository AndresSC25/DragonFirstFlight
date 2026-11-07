package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

internal const val CATAPULT_SPRITE_PATH = "sprites/boss/catapult/catapult_attack.png"
private const val FRAME_SIZE = 384
private const val FRAME_COUNT = 8

internal class CatapultView private constructor(
    private val image: Image,
) {
    fun update(snapshot: GameSnapshot) {
        val catapult = snapshot.catapult
        image.visible = catapult != null && !snapshot.isZoneTransitioning
        if (catapult == null) return

        image.x = catapult.x
        image.y = catapult.y
        image.scaleX = catapult.size / FRAME_SIZE
        image.scaleY = catapult.size / FRAME_SIZE
    }

    companion object {
        fun create(
            container: Container,
            spriteMap: Bitmap,
        ): CatapultView {
            require(spriteMap.width == FRAME_SIZE * FRAME_COUNT && spriteMap.height == FRAME_SIZE) {
                "Catapult spritesheet must be 3072x384, but was ${spriteMap.width}x${spriteMap.height}."
            }
            // Keep the resting pose until the attack sequence is implemented.
            val image =
                container.image(spriteMap.sliceWithSize(0, 0, FRAME_SIZE, FRAME_SIZE)) {
                    smoothing = false
                    visible = false
                }
            return CatapultView(image)
        }
    }
}
