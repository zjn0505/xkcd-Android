package xyz.jienan.xkcd.extra.fragment

import android.os.Bundle
import android.view.*
import kotlinx.android.synthetic.main.fab_sub_icons.*
import kotlinx.android.synthetic.main.fragment_comic_main.*
import xyz.jienan.xkcd.Const
import xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.extra.ExtraPagerAdapter
import xyz.jienan.xkcd.extra.contract.ExtraMainContract
import xyz.jienan.xkcd.extra.presenter.ExtraMainPresenter
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter
import xyz.jienan.xkcd.model.ExtraComics

class ExtraMainFragment : ContentMainBaseFragment(), ExtraMainContract.View {

    override val layoutResId = R.layout.fragment_comic_main

    override var searchHint = ""

    override val titleTextRes by lazy { getString(R.string.menu_extra) }

    override val presenter: ContentMainBasePresenter by lazy { ExtraMainPresenter(this) }

    override val adapter: BaseStatePagerAdapter by lazy { ExtraPagerAdapter(childFragmentManager) }

    override val pickerTitleTextRes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (viewPager != null && viewPager.currentItem >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.currentItem + 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_extra, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_go_xkcd).isVisible = false
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun showExtras(extraComics: List<ExtraComics>) {
        adapter.size = extraComics.size
        (adapter as ExtraPagerAdapter).setEntities(extraComics)
    }

    override fun suggestionClicked(position: Int) {
        // no-ops
    }

    override fun updateFab() {
        // no-ops
    }

    override fun onTabTitleDoubleTap() {
        logUXEvent(Const.FIRE_EXTRA_BOOKMARK_DOUBLE_TAP)
    }

    override fun onTabTitleLongPress() {
        logUXEvent(Const.FIRE_EXTRA_BOOKMARK_LONG_PRESS)
    }
}
