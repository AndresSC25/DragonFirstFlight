package com.redcrow.dragonsfirstflight

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity

internal fun createIntroSkipBackground(context: Context): Drawable {
    val bitmap =
        context.assets.open("ui/buttons/button_normal.png").use { input ->
            checkNotNull(BitmapFactory.decodeStream(input)).copy(Bitmap.Config.ARGB_8888, true)
        }
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    for (index in pixels.indices) {
        val pixel = pixels[index]
        if (Color.blue(pixel) > Color.red(pixel) && Color.blue(pixel) > Color.green(pixel)) {
            pixels[index] = Color.argb(Color.alpha(pixel), 248, 214, 109)
        }
    }
    bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    bitmap.density = Bitmap.DENSITY_NONE

    fun frame() = BitmapDrawable(context.resources, bitmap).apply { gravity = Gravity.FILL }
    val focusRing =
        GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke((3 * context.resources.displayMetrics.density).toInt(), Color.rgb(16, 24, 39))
        }
    val states =
        StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), LayerDrawable(arrayOf(frame(), focusRing)))
            addState(intArrayOf(), frame())
        }
    return RippleDrawable(ColorStateList.valueOf(Color.argb(64, 16, 24, 39)), states, frame())
}
