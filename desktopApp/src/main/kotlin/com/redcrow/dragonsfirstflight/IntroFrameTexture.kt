package com.redcrow.dragonsfirstflight

import korlibs.image.bitmap.Bitmap32
import java.util.concurrent.atomic.AtomicReference

internal class IntroFrameTexture(
    width: Int,
    height: Int,
) {
    val bitmap = Bitmap32(width, height, premultiplied = true)
    private val pending = AtomicReference<IntArray?>()

    @Volatile
    var hasVisiblePixels = false
        private set

    fun publishArgb(pixels: IntArray) {
        require(pixels.size == bitmap.ints.size)
        var visibleContent = hasVisiblePixels
        for (index in pixels.indices) {
            val argb = pixels[index]
            if ((argb and 0xffffff) != 0) visibleContent = true
            pixels[index] =
                (argb and -0x1000000) or
                ((argb and 0xff) shl 16) or
                (argb and 0xff00) or
                ((argb ushr 16) and 0xff)
        }
        hasVisiblePixels = visibleContent
        // Keep only the latest frame if decoding runs ahead of rendering.
        pending.set(pixels)
    }

    fun applyLatest(): Boolean {
        val pixels = pending.getAndSet(null) ?: return false
        bitmap.lock()
        try {
            pixels.copyInto(bitmap.ints)
        } finally {
            // KorGE must upload the changed pixels to OpenGL on the next render.
            bitmap.unlock()
        }
        return true
    }
}
