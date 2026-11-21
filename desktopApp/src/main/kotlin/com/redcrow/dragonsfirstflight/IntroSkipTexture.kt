package com.redcrow.dragonsfirstflight

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors

internal fun createIntroSkipTexture(source: Bitmap): Bitmap32 {
    val result = source.toBMP32().clone()
    val gold = Colors["#F8D66D"].value and 0xffffff
    for (index in result.ints.indices) {
        val pixel = result.ints[index]
        val red = pixel and 0xff
        val green = (pixel ushr 8) and 0xff
        val blue = (pixel ushr 16) and 0xff
        // Replace the blue interior, preserving the original gold frame and its transparency.
        if (blue > red && blue > green) {
            val alpha = pixel ushr 24
            val fill =
                if (result.premultiplied) {
                    ((248 * alpha + 127) / 255) or
                        (((214 * alpha + 127) / 255) shl 8) or
                        (((109 * alpha + 127) / 255) shl 16)
                } else {
                    gold
                }
            result.ints[index] = (pixel and -0x1000000) or fill
        }
    }
    return result
}
