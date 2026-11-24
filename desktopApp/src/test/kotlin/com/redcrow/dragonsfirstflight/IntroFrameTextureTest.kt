package com.redcrow.dragonsfirstflight

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IntroFrameTextureTest {
    @Test
    fun nativeFramesRespectRowPaddingAndColorChannels() {
        val buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
        for (pixel in intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0, 0xff0000ff.toInt(), -1, 0)) {
            buffer.putInt(pixel)
        }
        val texture = IntroFrameTexture(2, 2)

        texture.publishBgra(buffer, 2, 2, 12)

        assertTrue(texture.applyLatest())
        assertContentEquals(
            intArrayOf(0xff0000ff.toInt(), 0xff00ff00.toInt(), 0xffff0000.toInt(), -1),
            texture.bitmap.ints,
        )
        assertEquals(24, buffer.position())
    }

    @Test
    fun nativeFramesResizeIntoTheRenderTextureWithoutReadingPadding() {
        val buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
        for (pixel in intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0, 0xff0000ff.toInt(), -1, 0)) {
            buffer.putInt(pixel)
        }
        val texture = IntroFrameTexture(1, 2)

        texture.publishBgra(buffer, 2, 2, 12)

        assertTrue(texture.applyLatest())
        assertContentEquals(intArrayOf(0xff0000ff.toInt(), 0xffff0000.toInt()), texture.bitmap.ints)
    }

    @Test
    fun decodedColorsReachTheRenderBitmapAndInvalidateItsTexture() {
        val texture = IntroFrameTexture(3, 1)
        val bitmap = texture.bitmap
        val version = bitmap.contentVersion

        texture.publishArgb(intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt()))

        assertTrue(texture.applyLatest())
        assertSame(bitmap, texture.bitmap)
        assertContentEquals(
            intArrayOf(0xff0000ff.toInt(), 0xff00ff00.toInt(), 0xffff0000.toInt()),
            bitmap.ints,
        )
        assertTrue(bitmap.contentVersion > version)
        assertTrue(texture.hasVisiblePixels)
    }

    @Test
    fun slowRenderingUsesTheLatestFrameWithoutAccumulatingFrames() {
        val texture = IntroFrameTexture(1, 1)
        texture.publishArgb(intArrayOf(0xffff0000.toInt()))
        texture.publishArgb(intArrayOf(0xff00ff00.toInt()))

        assertTrue(texture.applyLatest())
        assertEquals(0xff00ff00.toInt(), texture.bitmap.ints.single())
        assertFalse(texture.applyLatest())
    }

    @Test
    fun emptyFrameDoesNotReportVisibleVideoContent() {
        val texture = IntroFrameTexture(1, 1)
        texture.publishArgb(intArrayOf(0xff000000.toInt()))
        assertTrue(texture.applyLatest())
        assertFalse(texture.hasVisiblePixels)
    }
}
