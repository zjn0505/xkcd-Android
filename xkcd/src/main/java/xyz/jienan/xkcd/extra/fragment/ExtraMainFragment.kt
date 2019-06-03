package xyz.jienan.xkcd.extra.fragment

import android.app.SearchManager
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fab_sub_icons.*
import kotlinx.android.synthetic.main.fragment_comic_main.*
import xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU
import xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.extra.ExtraPagerAdapter
import xyz.jienan.xkcd.extra.contract.ExtraMainContract
import xyz.jienan.xkcd.extra.presenter.ExtraMainPresenter
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment
import xyz.jienan.xkcd.list.activity.XkcdListActivity
import xyz.jienan.xkcd.model.ExtraComics

class ExtraMainFragment : ContentMainBaseFragment(), ExtraMainContract.View {
    override val layoutResId = R.layout.fragment_comic_main

    //    @BindString(R.string.search_hint_extra)
    public override var searchHint: String = "123"
//        internal set

//    @BindString(R.string.menu_extra)
    override var titleTextRes: String = "123"
//        internal set

    private var searchSuggestions: List<ExtraComics>? = null

    override// no-ops
    val pickerTitleTextRes: Int
        get() = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter = ExtraMainPresenter(this)
        adapter = ExtraPagerAdapter(childFragmentManager)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.visibility = View.GONE
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null && TextUtils.isEmpty(actionBar.subtitle)) {
            actionBar.subtitle = "1"
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (viewPager != null && viewPager.getCurrentItem() >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.getCurrentItem() + 1)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_xkcd_list -> {
                val intent = Intent(activity, XkcdListActivity::class.java)
                startActivityForResult(intent, ContentMainBaseFragment.REQ_LIST_ACTIVITY)
                logUXEvent(FIRE_BROWSE_LIST_MENU)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun renderXkcdSearch(xkcdPics: List<ExtraComics>) {
        searchSuggestions = xkcdPics
        val columns = arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA)
        val cursor = MatrixCursor(columns, xkcdPics.size)
        //        for (int i = 0; i < searchSuggestions.size(); i++) {
        //            XkcdPic xkcdPic = searchSuggestions.get(i);
        //            String[] tmp = {Integer.toString(i), xkcdPic.getTargetImg(), xkcdPic.getTitle(), String.valueOf(xkcdPic.num)};
        //            cursor.addRow(tmp);
        //        }
        searchAdapter!!.swapCursor(cursor)
    }

    override fun showExtras(extraComics: List<ExtraComics>) {
        adapter!!.setSize(extraComics.size)
        (adapter as ExtraPagerAdapter).setEntities(extraComics)
    }

    override fun suggestionClicked(position: Int) {
        //        if (searchSuggestions != null && searchSuggestions.size() > position) {
        //            XkcdPic xkcd = searchSuggestions.get(position);
        //            scrollViewPagerToItem((int) (xkcd.num - 1), false);
        //        }
    }

    override fun updateFab() {
        // no-ops
    }
}
