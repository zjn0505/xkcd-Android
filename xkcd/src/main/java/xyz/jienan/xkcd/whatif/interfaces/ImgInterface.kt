package xyz.jienan.xkcd.whatif.interfaces

import android.webkit.JavascriptInterface

class ImgInterface(private val imgCallback: ImgCallback?) {

    @JavascriptInterface
    fun doLongPress(title: String) {
        imgCallback?.onImgLongClick(title)
    }

    interface ImgCallback {
        fun onImgLongClick(title: String)
    }
}
