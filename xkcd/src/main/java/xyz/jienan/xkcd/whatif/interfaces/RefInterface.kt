package xyz.jienan.xkcd.whatif.interfaces

import android.webkit.JavascriptInterface

class RefInterface(private val callback: RefCallback?) {

    @JavascriptInterface
    fun refContent(refs: String) {
        callback?.onRefClick(refs)
    }

    interface RefCallback {
        fun onRefClick(content: String)
    }
}
