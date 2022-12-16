package xyz.jienan.xkcd.comics.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.ui.ToastUtils
import xyz.jienan.xkcd.ui.updateSettings
import java.util.*


class ImageWebViewActivity : BaseActivity() {

    companion object {

        const val TAG_INTERACTIVE = "interactive"

        const val TAG_XK3D = "xk3d"

        private const val PERMALINK_1663 = "xkcd_permalink_1663"

        private const val PERMALINK_2288 = "xkcd_permalink_2288"

        private const val TRIM = """
                    javascript:
                    var toDelete = [];
                    toDelete.push(document.getElementById("topContainer"));
                    toDelete.push(document.getElementById("bottom"));
                    toDelete.push(document.getElementById("ctitle"));
                    toDelete.push(document.getElementById("middleFooter"));
                    toDelete.push(document.getElementsByClassName("menuCont")[0]);
                    toDelete.push(document.getElementsByClassName("menuCont")[1]);
                    toDelete.push(document.getElementsByClassName("br")[0]);
                    toDelete.push(document.getElementsByClassName("br")[1]);
                    toDelete.push(document.getElementsByClassName("comicNav")[0]);
                    toDelete.push(document.getElementsByClassName("comicNav")[1]);
                    toDelete.forEach(function (element) {
                        if (element) element.style.display = 'none';
                    });
                """

        fun startActivity(context: Context, num: Long, translationMode: Boolean, webPageMode: String = TAG_INTERACTIVE) {
            val intent = Intent(context, ImageWebViewActivity::class.java)
            intent.putExtra("index", num)
            intent.putExtra("translationMode", translationMode)
            intent.putExtra("webPageMode", webPageMode)
            context.startActivity(intent)
        }
    }

    private val isInteractiveMode: Boolean
        get() = intent.getStringExtra("webPageMode") == TAG_INTERACTIVE

    private val webView by lazy { findViewById<WebView>(R.id.imgWebView) }

    private val progress by lazy { findViewById<ProgressBar>(R.id.progressWebView) }

    private val consoleView by lazy {
        TextView(this).also {
            it.movementMethod = ScrollingMovementMethod()
            it.gravity = Gravity.BOTTOM
            it.setBackgroundColor(Color.GRAY)
            it.setTextColor(Color.BLACK)
        }
    }

    private val consoleLogs = mutableListOf<String>()

    private val compositeDisposable = CompositeDisposable()

    private var permalink1663: String?
        get() = sharedPreferences.getString(PERMALINK_1663, "")
        set(value) = sharedPreferences.edit { putString(PERMALINK_1663, value) }

    private var permalink2288: String?
        get() = sharedPreferences.getString(PERMALINK_2288, "")
        set(value) = sharedPreferences.edit { putString(PERMALINK_2288, value) }

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

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        webView?.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (index !in listOf(1608L, 2445, 2601)) {
            return false
        }
        when (index) {
            1608L -> {
                listOf(
                        "i.am.gandalf",
                        "i.am.stuck",
                        "ze.goggles",
                        "coins.count"
                )
            }
            2445L -> listOf("console")
            2601L -> listOf("audio")
            else -> listOf()
        }.forEachIndexed { index, title ->
            menu.add(Menu.NONE, Menu.NONE, index, title)
        }

        return true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> {
                if (index == 1608L)
                    when (item.order) {
                        0 -> webView.loadUrl("javascript:i.am.gandalf=true")
                        1 -> webView.loadUrl("javascript:explorer.pos.x=512271;explorer.pos.y=-550319;explorer.pos.xv=0;explorer.pos.yv=0;explorer.pos.dir=1")
                        2 -> webView.loadUrl("javascript:ze.goggles()")
                        3 -> webView.loadUrl("javascript:android.onData('coin', JSON.stringify(explorer.objects))")
                    }
                else if (index == 2445L) {
                    if (item.order == 0) {
                        val isConsoleOpen = webView.getTag(R.id.webView) as Boolean? == true
                        webView.setTag(R.id.webView, !isConsoleOpen)
                        if (!isConsoleOpen) {
                            val params = WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    500,
                                    WindowManager.LayoutParams.TYPE_APPLICATION,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                                    PixelFormat.TRANSLUCENT)
                            params.gravity = Gravity.BOTTOM
                            params.x = 0
                            params.y = 100
                            windowManager.addView(consoleView, params)
                            val sb = StringBuilder()
                            consoleLogs.forEach {
                                sb.append(it)
                                sb.append("\n")
                            }
                            consoleView.text = sb.toString()
                        } else {
                            windowManager.removeView(consoleView)
                        }
                    }
                }
                else if (index == 2601L) {
                    if (item.order == 0) {
                        webView.loadUrl("javascript:togglePlayer()")
                    }
                }
            }
        }
        return true
    }

    class AndroidInterface(private val webView: WebView) {

        data class Coin(@SerializedName("got") val got: Boolean)

        @Keep
        @JavascriptInterface
        fun onData(key: String?, value: String) {
            if (key == "coin") {
                val coinListType = object : TypeToken<List<Coin>>() {}.type
                val coins = Gson().fromJson<List<Coin>>(value, coinListType)
                val score = "${coins.filter { it.got }.size}/${coins.size}"
                Timber.i("Coins $score")
                ToastUtils.showToast(webView.context, score)
            }
        }
    }

    private fun loadXkcdInWebView(xkcd: XkcdPic) {
        val urlScriptPair = if (isInteractiveMode) {
            loadInteractivePage(xkcd)
        } else {
            loadXk3dPage()
        }
        val url = urlScriptPair.first
        var script = urlScriptPair.second
        webView.addJavascriptInterface(AndroidInterface(webView), "android")
        webView.webViewClient = object : WebViewClient() {

            var retry = false

            override fun onPageFinished(view: WebView, url: String) {
                this@ImageWebViewActivity.title = view.title
                Timber.d("Current page $url")
                if (index == 1663L && url.contains("/1663/#".toRegex())) {
                    if (permalink1663.isNullOrBlank()) {
                        permalink1663 = extractUuidFrom1663url(url)
                    }
                } else if (index == 2288L && url.contains("https://xkcd.com/2288/#-".toRegex())) {
                    permalink2288 = url
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Timber.e("Failed to load $url,\n${error.errorCode} ${error.description}")
                    if (index == 2601L && error.errorCode == -1 && url.contains("github.io")) {
                        return
                    }
                }
                if (!retry && url.contains("github.io")) {
                    retry = true
                    script = TRIM.trimIndent()
                    webView.loadUrl("https://xkcd.com/${index}")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                val baseTitle = "xkcd: ${xkcd.title}"
                if (newProgress > 20 && script.isNotBlank()) view?.loadUrl(script)
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

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (index == 2445L) {
                    Timber.d("Console ${consoleMessage?.messageLevel()} ${consoleMessage?.message()}")
                    if (!consoleMessage?.message().isNullOrBlank() && consoleMessage?.message()?.startsWith("Ignored") == false) {
                        consoleLogs.add(consoleMessage.message())
                        if (consoleView.isVisible) {
                            val sb = StringBuilder()
                            consoleLogs.forEach {
                                sb.append(it)
                                sb.append("\n")
                            }
                            consoleView.text = sb.toString()
                        }
                    }
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }

        webView.updateSettings()
        if (index == 2288L) {
            webView.settings.displayZoomControls = true
        }
        webView.loadUrl(url)
    }

    private fun loadInteractivePage(xkcd: XkcdPic): Pair<String, String> {
        var script = ""
        val url = when (index.toInt()) {
            1663 -> {
                if (permalink1663.isNullOrBlank()) {
                    "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/"
                } else {
                    "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/#${permalink1663}"
                }
            }
            2288 -> {
                script = TRIM.trimIndent()
                if (permalink2288.isNullOrBlank()) {
                    "https://xkcd.com/2288"
                } else {
                    permalink2288!!
                }
            }
            1350,
            1506,
            1608,
            1975,
            2445,
            -> {
                script = TRIM.trimIndent()
                "https://xkcd.com/$index"
            }
            2601 -> {
                script = TRIM.trimIndent()
                "https://zjn0505.github.io/xkcd-2601-drawer/"
            }
            in resources.getIntArray(R.array.interactive_comics) -> if (!intent.getBooleanExtra("translationMode", false)) {
                "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/"
            } else {
                Timber.d("region ${Locale.getDefault()}")
                "https://zjn0505.github.io/xkcd-undressed/${xkcd.num}/?region=${Locale.getDefault().toString().replace("#", "_")}"
            }
            else -> {
                ""
            }
        }
        return url to script
    }

    private fun loadXk3dPage(): Pair<String, String> {
        // xk3d
        val script = """
                    javascript:
                    var toDelete = [];
                    toDelete.push(document.getElementById("topContainer"));
                    toDelete.push(document.getElementById("bottom"));
                    toDelete.push(document.getElementById("ctitle"));
                    toDelete.push(document.getElementById("middleFooter"));
                    toDelete.push(document.getElementsByClassName("menuCont")[0]);
                    toDelete.push(document.getElementsByClassName("menuCont")[1]);
                    toDelete.push(document.getElementsByClassName("br")[0]);
                    toDelete.push(document.getElementsByClassName("br")[1]);
                    toDelete.forEach(function (element) {
                        if (element) element.style.display = 'none';
                    });
                    var container = document.getElementById("container");
                    if (container) {
                        container.style.marginRight = 0;
                        container.style.marginLeft = 0;
                        container.style.width = "100vw";
                    }
                    var comic = document.getElementById("comic");
                    if (comic) {
                        comic.style.zoom = window.innerWidth / comic.offsetWidth / 1.2;
                    }
                """.trimIndent()
        val url = "https://xk3d.xkcd.com/$index"
        return url to script
    }

    @VisibleForTesting
    fun extractUuidFrom1663url(url: String) = url.substring(url.indexOf("/1663/#") + 7)
}