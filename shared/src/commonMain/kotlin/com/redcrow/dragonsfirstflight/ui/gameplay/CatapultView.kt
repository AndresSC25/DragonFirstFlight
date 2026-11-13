package com.redcrow.dragonsfirstflight.ui.gameplay

import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.sliceWithSize
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

internal const val CATAPULT_SPRITE_PATH = "sprites/boss/catapult/catapult_attack.png"
internal const val CATAPULT_WARNING_PATH = "sprites/boss/catapult/catapult_warning.png"
private const val FRAME_SIZE = 384
private const val FRAME_COUNT = 8
private const val WARNING_FRAME_SIZE = 64
private const val WARNING_SIZE = 80.0

internal class CatapultView private constructor(
    private val image: Image,
    private val frames: List<BmpSlice>,
    private val warning: Image,
    private val warningFrames: List<BmpSlice>,
) {
    fun update(snapshot: GameSnapshot) {
        val catapult = snapshot.catapult
        image.visible = catapult != null && !snapshot.isZoneTransitioning
        warning.visible = image.visible && catapult?.warningFrameIndex != null
        if (catapult == null) return

        image.bitmap = frames[catapult.frameIndex]
        image.x = catapult.x
        image.y = catapult.y
        image.scaleX = catapult.size / FRAME_SIZE
        image.scaleY = catapult.size / FRAME_SIZE
        val redIntensity = if (catapult.isFurious) 0.35 + 0.65 * catapult.furyPulse else 0.0
        val greenBlue = (255.0 - 145.0 * redIntensity).toInt()
        image.colorMul = RGBA(255, greenBlue, greenBlue, 255)

        catapult.warningFrameIndex?.let { frame ->
            warning.bitmap = warningFrames[frame]
            warning.x = catapult.x + (catapult.size - WARNING_SIZE) / 2.0
            warning.y = catapult.y + catapult.size * 0.30 - WARNING_SIZE - 12.0
        }
    }

    companion object {
        fun create(
            container: Container,
            spriteMap: Bitmap,
            warningMap: Bitmap,
        ): CatapultView {
            require(spriteMap.width == FRAME_SIZE * FRAME_COUNT && spriteMap.height == FRAME_SIZE) {
                "Catapult spritesheet must be 3072x384, but was ${spriteMap.width}x${spriteMap.height}."
            }
            require(warningMap.width == WARNING_FRAME_SIZE * FRAME_COUNT && warningMap.height == WARNING_FRAME_SIZE) {
                "Catapult warning spritesheet must be 512x64, but was ${warningMap.width}x${warningMap.height}."
            }
            val frames =
                List(FRAME_COUNT) { column ->
                    spriteMap.sliceWithSize(column * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE)
                }
            val warningFrames =
                List(FRAME_COUNT) { column ->
                    warningMap.sliceWithSize(column * WARNING_FRAME_SIZE, 0, WARNING_FRAME_SIZE, WARNING_FRAME_SIZE)
                }
            val image =
                container.image(frames[0]) {
                    smoothing = false
                    visible = false
                }
            val warning =
                container.image(warningFrames[0]) {
                    smoothing = false
                    scaleX = WARNING_SIZE / WARNING_FRAME_SIZE
                    scaleY = WARNING_SIZE / WARNING_FRAME_SIZE
                    visible = false
                }
            return CatapultView(image, frames, warning, warningFrames)
        }
    }
}
