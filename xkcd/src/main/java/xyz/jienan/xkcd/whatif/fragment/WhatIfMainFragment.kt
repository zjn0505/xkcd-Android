package xyz.jienan.xkcd.whatif.fragment

import android.app.SearchManager
import android.content.Intent
import android.database.MatrixCursor
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.jakewharton.rxbinding3.view.attaches
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fab_sub_icons.*
import kotlinx.android.synthetic.main.fragment_comic_main.*
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter
import xyz.jienan.xkcd.list.activity.WhatIfListActivity
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.whatif.WhatIfPagerAdapter
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract
import xyz.jienan.xkcd.whatif.presenter.WhatIfMainPresenter
import java.util.concurrent.TimeUnit

class WhatIfMainFragment : ContentMainBaseFragment(), WhatIfMainContract.View {

    override val layoutResId = R.layout.fragment_comic_main

    override val adapter: BaseStatePagerAdapter by lazy { WhatIfPagerAdapter(this) }

    override val presenter: ContentMainBasePresenter by lazy { WhatIfMainPresenter(this) }

    override var searchItemBackgroundRes: Int? = Color.parseColor("#EBEBEB")

    override val titleTextRes: String by lazy { resources.getString(R.string.menu_whatif) }

    override val searchHint: String by lazy { resources.getString(R.string.search_hint_what_if) }

    private lateinit var searchSuggestions: List<WhatIfArticle>

    private val compositeDisposable = CompositeDisposable()

    override val pickerTitleTextRes = R.string.dialog_pick_content_what_if

    override fun suggestionClicked(position: Int) {
        if (searchSuggestions.size > position) {
            val article = searchSuggestions[position]
            scrollViewPagerToItem((article.num - 1).toInt(), false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.attaches()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fab.hide() },
                        { e -> Timber.e(e, "fab observing error") })
                .let { compositeDisposable.add(it) }
        view.setBackgroundResource(R.drawable.what_if_webview_bg)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (viewPager != null && viewPager.currentItem >= 0) {
            outState.putInt(LAST_VIEW_WHAT_IF_ID, viewPager.currentItem + 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_what_if, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_what_if_list -> {
                val intent = Intent(activity, WhatIfListActivity::class.java)
                startActivityForResult(intent, REQ_LIST_ACTIVITY)
                logUXEvent(FIRE_BROWSE_LIST_MENU + FIRE_WHAT_IF_SUFFIX)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun latestWhatIfLoaded(whatIfArticle: WhatIfArticle) {
        latestIndex = whatIfArticle.num.toInt()
        super.latestLoaded()
    }

    override fun updateFab() {
        childFragmentManager.fragments
                .filter { it is SingleWhatIfFragment && it.ind == viewPager.currentItem + 1 }
                .forEach { (it as SingleWhatIfFragment).updateFab() }
    }

    override fun showFab(whatIfArticle: WhatIfArticle) {
        toggleFab(whatIfArticle.isFavorite)
        btnFav.isLiked = whatIfArticle.isFavorite
        btnThumb.isLiked = whatIfArticle.hasThumbed
        fab.show()
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    override fun showThumbUpCount(thumbCount: Long?) {
        showToast(requireContext(), thumbCount.toString())
    }

    override fun renderWhatIfSearch(articles: List<WhatIfArticle>) {
        searchSuggestions = articles
        val columns = arrayOf(BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA)
        val cursor = MatrixCursor(columns)

        searchSuggestions.forEachIndexed { index, article ->
            cursor.addRow(arrayOf(index.toString(), article.featureImg, article.title, article.num.toString()))
        }
        searchAdapter.swapCursor(cursor)
    }

    fun showOrHideFabWithInfo(isShowing: Boolean) {
        if (isShowing) {
            presenter.getInfoAndShowFab(currentIndex)
        } else {
            fab.hide()
            toggleSubFabs(false)
        }
    }

    override fun onTabTitleDoubleTap() {
        if (currentIndex > 0) {
            (presenter as WhatIfMainPresenter).setBookmark(currentIndex.toLong())
            showToast(requireContext(), getString(R.string.bookmark_saved))
            view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
            logUXEvent(FIRE_WHAT_IF_BOOKMARK_DOUBLE_TAP)
        }
    }

    override fun onTabTitleLongPress() {
        val index = (presenter as WhatIfMainPresenter).getBookmark().toInt()
        if (index > 0) {
            scrollViewPagerToItem(index - 1, true)
            view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
            logUXEvent(FIRE_WHAT_IF_BOOKMARK_LONG_PRESS)
        }
    }
}
