package xyz.jienan.xkcd.comics.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.ui.updateSettings


class ImageWebViewActivity : BaseActivity() {

    companion object {

        private const val PERMALINK_1663 = "xkcd_permalink_1663"

        private val githubIoHosts = arrayOf(826L, 1110L, 1416L, 1525L, 1608L, 1663L, 2067L, 2131L, 2198L)

        fun startActivity(context: Context, num: Long) {
            val intent = Intent(context, ImageWebViewActivity::class.java)
            intent.putExtra("index", num)
            context.startActivity(intent)
        }
    }

    private val webView by lazy { findViewById<WebView>(R.id.imgWebView) }

    private val progress by lazy { findViewById<ProgressBar>(R.id.progressWebView) }

    private val compositeDisposable = CompositeDisposable()

    private var permalink1663: String?
        get() = sharedPreferences.getString(PERMALINK_1663, "")
        set(value) = sharedPreferences.edit().putString(PERMALINK_1663, value).apply()

    private var index: Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_webview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        index = intent.getLongExtra("index", 1L)

        val xkcd = XkcdModel.loadXkcdFromDB(index)

        if (xkcd == null) {
            XkcdModel.loadXkcd(index)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .take(1)
                    .doOnNext { title = "xkcd: ${it.title}" }
                    .subscribe({ loadXkcdInWebView(it) }, { Timber.e(it) })
                    .also { compositeDisposable.add(it) }
        } else {
            title = "xkcd: ${xkcd.title}"
            loadXkcdInWebView(xkcd)
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    @SuppressLint("AddJavascriptInterface")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (index !in listOf(1608L)) {
            return false
        }
        when (index) {
            1608L -> menu?.add(Menu.NONE, R.id.menu_gandalf, Menu.NONE, "i.am.gandalf")
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_gandalf -> webView.loadUrl("javascript:i.am.gandalf=true")
        }
        return true
    }

    private fun loadXkcdInWebView(xkcd: XkcdPic) {
        var script = ""
        val url = when (xkcd.num) {
            1663L -> {
                if (permalink1663.isNullOrBlank()) {
                    "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/"
                } else {
                    "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/#${permalink1663}"
                }
            }
            in githubIoHosts -> "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/"
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
                "https://xkcd.com/${xkcd.num}"
            }
        }
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                this@ImageWebViewActivity.title = view.title
                if (script.isNotBlank()) {
                    view.loadUrl(script)
                }
                Timber.d("Current page $url")
                if (url.contains("/1663/#".toRegex())) {
                    if (permalink1663.isNullOrBlank()) {
                        permalink1663 = extractUuidFrom1663url(url)
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                val baseTitle = "xkcd: ${xkcd.title}"
                title = baseTitle + when (newProgress) {
                    in 0..99 -> {
                        progress.isIndeterminate = false
                        progress.progress = newProgress
                        " (${newProgress}%)"
                    }
                    else -> {
                        progress.visibility = View.GONE
                        ""
                    }
                }
            }
        }

        webView.updateSettings()
        webView.loadUrl(url)
    }

    @VisibleForTesting
    fun extractUuidFrom1663url(url: String) = url.substring(url.indexOf("/1663/#") + 7)
}