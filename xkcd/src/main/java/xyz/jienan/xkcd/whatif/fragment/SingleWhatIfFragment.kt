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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_what_if_single.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseFragment
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.model.util.appendCss
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

    var ind = -1

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setHasOptionsMenu(true)

        if (ind != -1) {
            webView.loadArticle(ind)

            webView.enableJsInterfaces()

            if (getParentFragment() is WhatIfMainFragment) {
                parentFragment = getParentFragment() as WhatIfMainFragment?
            }
        }
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
        if (parentFragment !is WhatIfMainFragment || (parentFragment as WhatIfMainFragment).currentIndex != ind) {
            return false
        }
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
        view?.performHapticFeedback(LONG_PRESS)
        logUXEvent(FIRE_WHAT_IF_IMG_LONG)
    }

    override fun onRefClick(content: String) {
        Observable.just(content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ showSimpleInfoDialog(it) },
                        { Timber.e(it, "ref click error") })
                .also { compositeDisposable.add(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view?.performHapticFeedback(CONTEXT_CLICK)
        }
        logUXEvent(FIRE_WHAT_IF_REF)
    }

    private fun showSimpleInfoDialog(content: String) {
        dialog = AlertDialog.Builder(requireContext()).create()
        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.dialog_explain, null) as LinearLayout
        val document = Jsoup.parse(content)
        document.convertImgToView().map { view.addView(it) }

        view.findViewById<TextView>(R.id.tvExplain).apply {
            text = HtmlCompat.fromHtml(document.html(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        dialog!!.setView(view)
        dialog!!.show()
    }

    private fun Element.convertImgToView() =
            select("img.illustration")
                    .map { element ->
                        element.remove()
                        element.absUrl("src")
                    }.map { url ->
                        val imgView = ImageView(context)
                        Glide.with(context).load(url).fitCenter().into(imgView)
                        imgView
                    }

    private inner class WebViewScrollCallback internal constructor(fragment: SingleWhatIfFragment)
        : WhatIfWebView.ScrollToEndCallback {

        private val weakReference: WeakReference<SingleWhatIfFragment> = WeakReference(fragment)

        override fun scrolledToTheEnd(isTheEnd: Boolean) {
            val fragment = weakReference.get() ?: return
            fragment.scrolledToTheEnd(isTheEnd)
        }
    }

    private fun WhatIfWebView.loadArticle(index: Int) {
        WhatIfModel.loadArticle(index.toLong())
                .doOnSuccess { WhatIfModel.push(it) }
                .map {
                    if (context?.getUiNightModeFlag() == Configuration.UI_MODE_NIGHT_YES) {
                        val doc = Jsoup.parse(it.content)
                        doc.head()!!.appendCss("night_style.css")
                        it.content = doc.html()
                    }
                    it
                }
                .subscribe({
                    loadDataWithBaseURL("file:///android_asset/",
                            it.content?.replace("\\$".toRegex(), "&#36;"),
                            "text/html",
                            "UTF-8",
                            null)
                }, { Timber.e(it) })
                .let { compositeDisposable.add(it) }
    }

    @SuppressLint("AddJavascriptInterface")
    private fun WhatIfWebView.enableJsInterfaces() {
        setCallback(WebViewScrollCallback(this@SingleWhatIfFragment))
        setLatexScrollInterface(latexInterface)
        addJavascriptInterface(latexInterface, "AndroidLatex")
        addJavascriptInterface(ImgInterface(this@SingleWhatIfFragment), "AndroidImg")
        addJavascriptInterface(RefInterface(this@SingleWhatIfFragment), "AndroidRef")
    }

    companion object {

        fun newInstance(articleId: Int) =
                SingleWhatIfFragment().apply { arguments = bundleOf("ind" to articleId) }
    }
}