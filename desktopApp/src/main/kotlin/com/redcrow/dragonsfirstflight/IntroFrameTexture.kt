package com.redcrow.dragonsfirstflight

import korlibs.image.bitmap.Bitmap32
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

    fun publishBgra(
        buffer: ByteBuffer,
        sourceWidth: Int,
        sourceHeight: Int,
        strideBytes: Int,
    ) {
        require(sourceWidth > 0 && sourceHeight > 0 && strideBytes >= sourceWidth * 4 && strideBytes % 4 == 0)
        val bytes = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        bytes.clear()
        require(bytes.limit().toLong() >= (sourceHeight - 1L) * strideBytes + sourceWidth * 4L)
        val source = bytes.asIntBuffer()
        val stride = strideBytes / 4
        val pixels = IntArray(bitmap.width * bitmap.height)
        for (y in 0 until bitmap.height) {
            val sourceRow = (y * sourceHeight / bitmap.height) * stride
            val targetRow = y * bitmap.width
            for (x in 0 until bitmap.width) {
                pixels[targetRow + x] = source.get(sourceRow + x * sourceWidth / bitmap.width)
            }
        }
        publishArgb(pixels)
    }

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
