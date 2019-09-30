package xyz.jienan.xkcd.ui.like

import androidx.annotation.DrawableRes

/**
 * Created by Joel on 23/12/2015.
 */
class Icon internal constructor(@DrawableRes val onIconResourceId: Int,
                                @DrawableRes val offIconResourceId: Int,
                                val iconType: IconType)