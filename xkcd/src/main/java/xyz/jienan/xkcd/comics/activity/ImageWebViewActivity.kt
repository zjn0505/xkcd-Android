package xyz.jienan.xkcd.comics.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.model.persist.SharedPrefManager


class ImageWebViewActivity : BaseActivity() {

    companion object {

        private val githubIoHosts = arrayOf(1663L, 2067L, 2131L, 2198L)

        fun startActivity(context: Context, num: Long) {
            val intent = Intent(context, ImageWebViewActivity::class.java)
            intent.putExtra("index", num)
            context.startActivity(intent)
        }
    }

    private val webView by lazy { WebView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(webView)
        var script = ""
        val url = when (val index = intent.getLongExtra("index", 1)) {
            in githubIoHosts -> "https://zjn0505.github.io/xkcd-undressed/${index}/"
            else -> {
                script = """
                    javascript:
                    var toDelete = [];
                    toDelete.push(document.getElementById("topContainer"));
                    toDelete.push(document.getElementById("bottom"));
                    toDelete.push(document.getElementById("ctitle"));
                    toDelete.push(document.getElementsByClassName("comicNav")[0]);
                    toDelete.push(document.getElementsByClassName("comicNav")[1]);
                    toDelete.push(document.getElementsByClassName("br")[0]);
                    toDelete.push(document.getElementsByClassName("br")[1]);
                    document.getElementById("middleContainer").getElementsByTagName('br')[0].nextSibling.textContent = "";
                    document.getElementById("middleContainer").getElementsByTagName('br')[1].nextSibling.textContent = "";
                    toDelete.forEach(function(element) {element.style.display = 'none';});
                """.trimIndent()
                "https://xkcd.com/$index"
            }
        }
        if (script.isNotBlank()) {
            webView.visibility = View.GONE
            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView, url: String) {
                    view.loadUrl(script)
                    view.visibility = View.VISIBLE
                }
            }

        }
        webView.loadUrl(url)
        updateSettings()
    }

    private fun updateSettings() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }

        webView.settings.apply {
            builtInZoomControls = true
            useWideViewPort = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            @SuppressLint("SetJavaScriptEnabled")
            javaScriptEnabled = true
            displayZoomControls = false
            loadWithOverviewMode = true
            allowFileAccess = true
            setAppCacheEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            textZoom = SharedPrefManager.whatIfZoom
        }
    }
}