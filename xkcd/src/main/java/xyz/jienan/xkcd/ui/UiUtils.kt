package xyz.jienan.xkcd.ui

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getUiNightModeFlag() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

fun dip2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}