package xyz.jienan.xkcd.ui.like

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import xyz.jienan.xkcd.R

/**
 * Created by Joel on 23/12/2015.
 */

internal val icons = listOf(Icon(R.drawable.ic_heart_on, R.drawable.ic_heart_off, IconType.HEART),
        Icon(R.drawable.ic_thumb_on, R.drawable.ic_thumb_off, IconType.THUMB))

internal fun Float.mapValueFromRangeToRange(fromLow: Float, fromHigh: Float, toLow: Float, toHigh: Float): Float {
    return toLow + (this - fromLow) / (fromHigh - fromLow) * (toHigh - toLow)
}

internal fun Drawable.resizeDrawable(context: Context, width: Int, height: Int): Drawable {
    return toBitmap(width = width, height = height, config = Bitmap.Config.ARGB_8888)
            .scale(width, height, true)
            .toDrawable(context.resources)
}
