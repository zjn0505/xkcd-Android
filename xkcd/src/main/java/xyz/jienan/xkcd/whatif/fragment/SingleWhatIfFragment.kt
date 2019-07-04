package xyz.jienan.xkcd.whatif.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.HapticFeedbackConstants.*
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_what_if_single.*
import org.jsoup.Jsoup
import timber.log.Timber
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseFragment
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.WhatIfWebView
import xyz.jienan.xkcd.ui.getUiNightModeFlag
import xyz.jienan.xkcd.whatif.interfaces.ImgInterface
import xyz.jienan.xkcd.whatif.interfaces.LatexInterface
import xyz.jienan.xkcd.whatif.interfaces.RefInterface
import java.lang.ref.WeakReference

/**
 * Created by jienanzhang on 03/03/2018.
 */

open class SingleWhatIfFragment : BaseFragment(), ImgInterface.ImgCallback, RefInterface.RefCallback {

    private var ind = -1

    private var parentFragment: WhatIfMainFragment? = null

    private val latexInterface = LatexInterface()

    protected var compositeDisposable = CompositeDisposable()

    private var dialog: AlertDialog? = null

    protected var sharedPref = SharedPrefManager

    override val layoutResId = R.layout.fragment_what_if_single

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null)
            ind = args.getInt("ind", -1)
        setHasOptionsMenu(true)
    }

    @SuppressLint("AddJavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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
            textZoom = sharedPref.whatIfZoom
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }
        if (ind != -1) {
            WhatIfModel.loadArticle(ind.toLong())
                    .doOnSuccess { WhatIfModel.push(it) }
                    .map {
                        if (context?.getUiNightModeFlag() == Configuration.UI_MODE_NIGHT_YES) {
                            val doc = Jsoup.parse(it.content)
                            doc.head().appendElement("link")
                                    .attr("rel", "stylesheet")
                                    .attr("type", "text/css")
                                    .attr("href", "night_style.css")

                            it.content = doc.html()
                        }
                        it
                    }
                    .subscribe({
                        webView?.loadDataWithBaseURL("file:///android_asset/",
                                it.content?.replace("\\$".toRegex(), "&#36;"),
                                "text/html",
                                "UTF-8",
                                null)
                    }, { Timber.e(it) })
                    .let { compositeDisposable.add(it) }

            webView.apply {
                setCallback(WebViewScrollCallback(this@SingleWhatIfFragment))
                setLatexScrollInterface(latexInterface)
                addJavascriptInterface(latexInterface, "AndroidLatex")
                addJavascriptInterface(ImgInterface(this@SingleWhatIfFragment), "AndroidImg")
                addJavascriptInterface(RefInterface(this@SingleWhatIfFragment), "AndroidRef")
            }

            if (getParentFragment() is WhatIfMainFragment) {
                parentFragment = getParentFragment() as WhatIfMainFragment?
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }
        setHasOptionsMenu(true)
        WhatIfModel.observeZoom()
                .subscribe({ zoom -> webView.settings.textZoom = zoom!! },
                        { e -> Timber.e(e, "observing zoom error") })
                .let { compositeDisposable.add(it) }
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        compositeDisposable.dispose()
        Timber.d("OkHttp: clear $ind")
        webView.clearTasks()
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_what_if, "https://whatif.xkcd.com/$ind"))
                    type = "text/plain"
                    startActivity(Intent.createChooser(this, resources.getText(R.string.share_to_what_if)))
                }
                logUXEvent(FIRE_SHARE_BAR + FIRE_WHAT_IF_SUFFIX)
            }
            R.id.action_go_xkcd -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://whatif.xkcd.com/$ind")))
                logUXEvent(FIRE_GO_WHAT_IF_MENU)
            }
        }
        return true
    }

    fun updateFab() {
        if (webView.distanceToEnd() < 200) {
            scrolledToTheEnd(true)
        } else if (webView.distanceToEnd() > 250) {
            scrolledToTheEnd(false)
        }
    }

    private fun scrolledToTheEnd(isTheEnd: Boolean) {
        if (parentFragment != null) {
            if (!parentFragment!!.isFabShowing && isTheEnd) {
                parentFragment!!.showOrHideFabWithInfo(true)
            } else if (parentFragment!!.isFabShowing && !isTheEnd) {
                parentFragment!!.showOrHideFabWithInfo(false)
            }
        }
    }

    override fun onImgLongClick(title: String) {
        Observable.just(title)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ showSimpleInfoDialog(it) },
                        { Timber.e(it, "long click error") })
                .also { compositeDisposable.add(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
        }
        logUXEvent(FIRE_WHAT_IF_IMG_LONG)
    }

    override fun onRefClick(content: String) {
        Observable.just(content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ showSimpleInfoDialog(it) },
                        { Timber.e(it, "ref click error") })
                .also { compositeDisposable.add(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view?.performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING)
        }
        logUXEvent(FIRE_WHAT_IF_REF)
    }

    private fun showSimpleInfoDialog(content: String) {
        dialog = AlertDialog.Builder(context!!).create()
        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.dialog_explain, null) as LinearLayout
        val document = Jsoup.parse(content).also {
            it.select("img.illustration").forEach { element ->
                element.remove()
                ImageView(context).also { iv ->
                    Glide.with(context).load(element.absUrl("src")).fitCenter().into(iv)
                    view.addView(iv)
                }
            }
        }
        view.findViewById<TextView>(R.id.tvExplain).apply {
            text = HtmlCompat.fromHtml(document.html(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        dialog!!.setView(view)
        dialog!!.show()
    }

    private inner class WebViewScrollCallback internal constructor(fragment: SingleWhatIfFragment)
        : WhatIfWebView.ScrollToEndCallback {

        private val weakReference: WeakReference<SingleWhatIfFragment> = WeakReference(fragment)

        override fun scrolledToTheEnd(isTheEnd: Boolean) {
            val fragment = weakReference.get() ?: return
            fragment.scrolledToTheEnd(isTheEnd)
        }
    }

    companion object {

        fun newInstance(articleId: Int) =
                SingleWhatIfFragment().apply { arguments = Bundle(1).apply { putInt("ind", articleId) } }
    }
}
