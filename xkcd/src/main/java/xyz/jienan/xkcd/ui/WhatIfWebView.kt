package xyz.jienan.xkcd.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.FutureTarget
import timber.log.Timber
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity
import xyz.jienan.xkcd.model.util.XkcdExplainUtil
import xyz.jienan.xkcd.whatif.interfaces.LatexInterface
import java.io.File
import java.io.FileInputStream
import java.util.*

class WhatIfWebView(context: Context, attrs: AttributeSet) : WebView(context, attrs) {

    private var callback: ScrollToEndCallback? = null

    private var latexScrollInterface: LatexInterface? = null

    private val glide: RequestManager

    private val imageTasks = ArrayList<FutureTarget<File>>()

    init {

        val nightModeFlags = context.getUiNightModeFlag()

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            setBackgroundColor(Color.parseColor("#3A3A3A"))
        } else {
            setBackgroundColor(Color.TRANSPARENT)
        }

        glide = Glide.with(context)
        webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                var url = url
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && url.contains("what-if.xkcd.com/imgs/a/") || url.contains("imgs.xkcd.com/comics")) {
                    url = url.replace("http://", "https://")
                    val t = glide.load(url).downloadOnly(10, 10)
                    imageTasks.add(t)
                    try {
                        return WebResourceResponse("image/png", "deflate", FileInputStream(t.get()))
                    } catch (e: Exception) {
                        Timber.e(e)
                    }

                    return null
                } else {
                    return super.shouldInterceptRequest(view, url)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (XkcdExplainUtil.isXkcdImageLink(url)) {
                    val id = XkcdExplainUtil.getXkcdIdFromXkcdImageLink(url)
                    if (id != -1L) {
                        ImageDetailPageActivity.startActivityFromId(getContext(), id)
                    } else {
                        ImageDetailPageActivity.startActivity(getContext(), url, 1L)
                    }
                    if (getContext() is Activity) {
                        (getContext() as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    }

                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    if (browserIntent.resolveActivity(view.context.packageManager) != null) {
                        view.context.startActivity(browserIntent)
                    }
                }
                return true
            }
        }
    }

    fun setLatexScrollInterface(latexScrollInterface: LatexInterface) {
        this.latexScrollInterface = latexScrollInterface
    }

    fun setCallback(callback: ScrollToEndCallback) {
        this.callback = callback
    }

    fun distanceToEnd(): Int {
        val height = Math.floor((this.contentHeight * this.scale).toDouble()).toInt()
        val webViewHeight = this.measuredHeight
        return height - scrollY - webViewHeight
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (callback != null) {
            if (distanceToEnd() < 200) {
                callback!!.scrolledToTheEnd(true)
            } else if (distanceToEnd() > 250) {
                callback!!.scrolledToTheEnd(false)
            }
        }
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun destroy() {
        callback = null
        super.destroy()
    }

    fun canScrollHor(direction: Int): Boolean {
        if (zoomScroll(direction)) {
            return true
        }
        return if (latexScrollInterface != null) {
            !latexScrollInterface!!.canScrollHor()
        } else zoomScroll(direction)
    }

    private fun zoomScroll(direction: Int): Boolean {
        val offset = computeHorizontalScrollOffset()
        val range = computeHorizontalScrollRange() - computeHorizontalScrollExtent()
        if (range == 0) return false
        return if (direction < 0) {
            offset > 0
        } else {
            offset < range - 1
        }
    }

    fun clearTasks() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            for (t in imageTasks) {
                t.cancel(true)
                t.clear()
                Timber.e("OkHttp: cleared tasks %d", imageTasks.size)
            }
        }
    }

    interface ScrollToEndCallback {
        fun scrolledToTheEnd(isTheEnd: Boolean)
    }
}
