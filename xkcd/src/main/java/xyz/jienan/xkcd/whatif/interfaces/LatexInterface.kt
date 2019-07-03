package xyz.jienan.xkcd.whatif.interfaces

import android.webkit.JavascriptInterface

class LatexInterface {

    private var canParentScroll = true

    @JavascriptInterface
    fun onTouch(i: Int) {
        canParentScroll = i == 3
    }

    fun canScrollHor() = canParentScroll
}
