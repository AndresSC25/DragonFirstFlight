package com.redcrow.dragonsfirstflight

import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class IntroSkipTextureTest {
    @Test
    fun theEntireBlueInteriorBecomesGoldWhileTheMenuAssetStaysUnchanged() {
        val source = Bitmap32(4, 1)
        val original =
            intArrayOf(
                Colors["#000033"].value,
                Colors["#000066"].value,
                Colors["#805020"].value,
                Colors.TRANSPARENT.value,
            )
        original.copyInto(source.ints)

        val result = createIntroSkipTexture(source)

        assertContentEquals(original, source.ints)
        assertEquals(Colors["#F8D66D"].value, result.ints[0])
        assertEquals(result.ints[0], result.ints[1])
        assertEquals(original[2], result.ints[2])
        assertEquals(original[3], result.ints[3])
    }
}
