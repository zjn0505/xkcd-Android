package xyz.jienan.xkcd.extra.fragment

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.fragment_extra_single.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.ExtraModel
import xyz.jienan.xkcd.model.util.appendCss
import xyz.jienan.xkcd.ui.getUiNightModeFlag
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment
import kotlin.math.abs

/**
 * Created by jienanzhang on 03/03/2018.
 */

class SingleExtraWebViewFragment : SingleWhatIfFragment() {

    private val extraComics by lazy { arguments?.getSerializable("entity") as ExtraComics }

    private var currentPage = 0

    override val layoutResId = R.layout.fragment_extra_single

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customWebViewSettings()

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current", 0)
        }
        loadLinkPage(currentPage)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        if (webView != null) {
            webView.clearTasks()
        }
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if ((parentFragment as ExtraMainFragment).currentIndex != 1) { // use 0 as we only have 1 extra WebView at first page
            return false
        }
        when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_article_extra, extraComics.links?.get(0)))
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_to)))
                logUXEvent(FIRE_SHARE_BAR + FIRE_EXTRA_SUFFIX)
                return true
            }
            R.id.action_go_explain -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(extraComics.explainUrl))
                startActivity(browserIntent)
                logUXEvent(FIRE_GO_EXTRA_MENU)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current", currentPage)
    }

    private fun loadLinkPage(pageIndex: Int) {
        val links = extraComics.links
        if (links != null) {
            ExtraModel.parseContentFromUrl(links[abs(pageIndex % links.size)])
                    .map {
                        if (context?.getUiNightModeFlag() == Configuration.UI_MODE_NIGHT_YES) {
                            val doc = Jsoup.parse(it)
                            doc.head().appendCss("night_style.css")
                            doc.html()
                        } else {
                            it
                        }
                    }
                    .map {
                        // Check solution / Go back to puzzle,
                        val doc = Jsoup.parse(it)
                        val (text, uri) =  if (pageIndex == 0) {
                            getString(R.string.check_solution) to  URI_XKCD_EXTRA_SOLUTION
                        } else  {
                            getString(R.string.check_puzzle) to URI_XKCD_EXTRA_PUZZLE
                        }
                        doc.body().children().last().appendElement("br").appendChild(Element("a").appendText(text).attr("href", uri))
                        doc.html()
                    }
                    .subscribe({ html ->
                        webView.loadDataWithBaseURL("file:///android_asset/.",
                                html, "text/html", "UTF-8", null)

                        webView.addXkcdUriInterceptor { url ->
                            if (url == URI_XKCD_EXTRA_SOLUTION) {
                                currentPage = 1
                                loadLinkPage(currentPage)
                            } else if (url == URI_XKCD_EXTRA_PUZZLE) {
                                currentPage = 0
                                loadLinkPage(currentPage)
                            }
                        }
                    }, { Timber.e(it) })
                    .also { compositeDisposable.add(it) }
        }
    }

    private fun customWebViewSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.settings.textZoom = (sharedPref.whatIfZoom * 1.5).toInt()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        }
    }

    companion object {
        fun newInstance(extraComics: ExtraComics) =
                SingleExtraWebViewFragment().apply { arguments = bundleOf("entity" to extraComics) }
    }
}
