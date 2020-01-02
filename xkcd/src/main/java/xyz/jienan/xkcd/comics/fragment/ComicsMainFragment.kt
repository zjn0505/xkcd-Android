package xyz.jienan.xkcd.comics.fragment

import android.app.SearchManager
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import kotlinx.android.synthetic.main.fab_sub_icons.*
import kotlinx.android.synthetic.main.fragment_comic_main.*
import xyz.jienan.xkcd.Const
import xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU
import xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.comics.ComicsPagerAdapter
import xyz.jienan.xkcd.comics.contract.ComicsMainContract
import xyz.jienan.xkcd.comics.presenter.ComicsMainPresenter
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter
import xyz.jienan.xkcd.list.activity.XkcdListActivity
import xyz.jienan.xkcd.model.XkcdPic

class ComicsMainFragment : ContentMainBaseFragment(), ComicsMainContract.View {

    override val layoutResId = R.layout.fragment_comic_main

    public override val searchHint: String by lazy { resources.getString(R.string.search_hint_xkcd) }

    override val titleTextRes: String by lazy { resources.getString(R.string.menu_xkcd) }

    private lateinit var searchSuggestions: List<XkcdPic>

    override val presenter: ContentMainBasePresenter by lazy { ComicsMainPresenter(this) }

    override val adapter: BaseStatePagerAdapter by lazy { ComicsPagerAdapter(childFragmentManager) }

    override val pickerTitleTextRes = R.string.dialog_pick_content

    override fun latestXkcdLoaded(xkcdPic: XkcdPic) {
        latestIndex = xkcdPic.num.toInt()
        super.latestLoaded()
        (presenter as ComicsMainPresenter).fastLoad(latestIndex)
    }

    override fun showFab(xkcdPic: XkcdPic) {
        xkcdPic.apply {
            toggleFab(isFavorite)
            btnFav.isLiked = isFavorite
            btnThumb.isLiked = hasThumbed
        }
        fab?.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (viewPager != null && viewPager.currentItem >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.currentItem + 1)
        }
    }

    override fun updateFab() {
        presenter.getInfoAndShowFab(currentIndex)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_xkcd, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_xkcd_list -> {
                val intent = Intent(activity, XkcdListActivity::class.java)
                startActivityForResult(intent, REQ_LIST_ACTIVITY)
                logUXEvent(FIRE_BROWSE_LIST_MENU)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showThumbUpCount(thumbCount: Long?) {
        showToast(context!!, thumbCount.toString())
    }

    override fun renderXkcdSearch(xkcdPics: List<XkcdPic>) {
        searchSuggestions = xkcdPics
        val columns = arrayOf(BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA)
        val cursor = MatrixCursor(columns, xkcdPics.size)

        searchSuggestions.forEachIndexed { index, xkcdPic ->
            cursor.addRow(arrayOf(index.toString(), xkcdPic.targetImg, xkcdPic.title, xkcdPic.num.toString()))
        }

        searchAdapter.swapCursor(cursor)
    }

    override fun suggestionClicked(position: Int) {
        if (searchSuggestions.size > position) {
            val xkcd = searchSuggestions[position]
            scrollViewPagerToItem((xkcd.num - 1).toInt(), false)
        }
    }

    override fun onTabTitleDoubleTap() {
        if (currentIndex > 0) {
            (presenter as ComicsMainPresenter).bookmark = currentIndex.toLong()
            showToast(context!!, getString(R.string.bookmark_saved))
            view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
            logUXEvent(Const.FIRE_XKCD_BOOKMARK_DOUBLE_TAP)
        }
    }

    override fun onTabTitleLongPress() {
        val index = (presenter as ComicsMainPresenter).bookmark.toInt()
        if (index > 0) {
            scrollViewPagerToItem(index - 1, true)
            view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
            logUXEvent(Const.FIRE_XKCD_BOOKMARK_LONG_PRESS)
        }
    }
}