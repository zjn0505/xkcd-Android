package xyz.jienan.xkcd.list.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_list.*
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.list.ListFilterDialogFragment
import xyz.jienan.xkcd.list.XkcdListGridAdapter
import xyz.jienan.xkcd.list.activity.XkcdListActivity.Selection.*
import xyz.jienan.xkcd.list.contract.XkcdListContract
import xyz.jienan.xkcd.list.presenter.XkcdListPresenter
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.ui.RecyclerItemClickListener

/**
 * Created by jienanzhang on 22/03/2018.
 */

class XkcdListActivity : BaseActivity(), XkcdListContract.View, ListFilterDialogFragment.OnItemSelectListener {

    private val mAdapter by lazy { XkcdListGridAdapter() }

    private val sglm by lazy { StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL) }

    private var loadingMore = false

    private var currentSelection = ALL_COMICS

    private val xkcdListPresenter by lazy { XkcdListPresenter(this) }

    private val rvScrollListener = object : RecyclerView.OnScrollListener() {

        private val FLING_JUMP_LOW_THRESHOLD = 80
        private val FLING_JUMP_HIGH_THRESHOLD = 120

        private var dragging = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            dragging = newState == SCROLL_STATE_DRAGGING
            if (mAdapter.pauseLoading) {
                if (newState == SCROLL_STATE_DRAGGING || newState == SCROLL_STATE_IDLE) {
                    // user is touchy or the scroll finished, show images
                    mAdapter.pauseLoading = false
                } // settling means the user let the screen go, but it can still be flinging
            }

            if (!rvList!!.canScrollVertically(1) && lastItemReached() && newState == SCROLL_STATE_IDLE) {
                logUXEvent(FIRE_SCROLL_TO_END)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!dragging) {
                val currentSpeed = Math.abs(dy)
                val paused = mAdapter.pauseLoading
                if (paused && currentSpeed < FLING_JUMP_LOW_THRESHOLD) {
                    mAdapter.pauseLoading = false
                } else if (!paused && FLING_JUMP_HIGH_THRESHOLD < currentSpeed) {
                    mAdapter.pauseLoading = true
                }
            }
            if (currentSelection != ALL_COMICS) {
                return
            }
            val visibleItemCount = sglm.childCount
            var firstVisibleItemPositions = IntArray(spanCount)
            firstVisibleItemPositions = sglm.findFirstVisibleItemPositions(firstVisibleItemPositions)
            if (firstVisibleItemPositions[1] + visibleItemCount >= mAdapter.itemCount - COUNT_IN_ADV
                    && !loadingMore
                    && !lastItemReached()) {
                loadingMore = true
                xkcdListPresenter.loadList((mAdapter.pics!![mAdapter.itemCount - 1].num + 1).toInt())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentSelection != ALL_COMICS) {
            outState.putInt("Selection", currentSelection.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (xkcdListPresenter.hasFav()) {
            menuInflater.inflate(R.menu.menu_list, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_filter -> {
                val fragmentManager = supportFragmentManager
                var filterDialog = fragmentManager.findFragmentByTag("filter") as ListFilterDialogFragment?
                if (filterDialog == null) {
                    filterDialog = ListFilterDialogFragment()
                }
                val filters = intArrayOf(R.string.filter_all_comics, R.string.filter_my_fav, R.string.filter_people_choice)
                filterDialog.setFilters(filters)
                if (!filterDialog.isAdded) {
                    filterDialog.show(supportFragmentManager, "filter")
                    filterDialog.setItemSelectListener(this)
                    filterDialog.setSelection(currentSelection.ordinal)
                    logUXEvent(FIRE_LIST_FILTER_BAR)
                }
            }
        }
        return true
    }

    private fun reloadList(currentSelection: Selection) {
        when (currentSelection) {
            ALL_COMICS -> xkcdListPresenter.loadList(1)
            MY_FAVORITE -> xkcdListPresenter.loadFavList()
            PEOPLES_CHOICE -> xkcdListPresenter.loadPeopleChoiceList()
        }
        rvList!!.scrollToPosition(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        rvScroller?.setRecyclerView(rvList)
        rvScroller?.setViewsToUse(R.layout.rv_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle)
        rvList?.adapter = mAdapter
        rvList?.setHasFixedSize(true)
        rvList?.addOnItemTouchListener(RecyclerItemClickListener(this, rvList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (position >= 0) {
                    val intent = Intent()
                    intent.putExtra(INTENT_TARGET_XKCD_ID, mAdapter.getPic(position)?.num?.toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            override fun onLongItemClick(view: View, position: Int) {
                // no-ops
            }
        }))
        rvList?.layoutManager = sglm
        rvList?.addOnScrollListener(rvScrollListener)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            val selection = savedInstanceState.getInt("Selection", ALL_COMICS.id)
            currentSelection = Selection.fromValue(selection)
        } else {
            sharedPreferences.edit().putInt("FILTER_SELECTION", ALL_COMICS.id).apply()
        }
        reloadList(currentSelection)
    }

    override fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbLoading?.visibility = View.VISIBLE
            rvList?.visibility = View.GONE
        } else {
            pbLoading?.visibility = View.GONE
            rvList?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        rvList?.removeOnScrollListener(rvScrollListener)
        xkcdListPresenter.onDestroy()
        super.onDestroy()
    }

    private fun lastItemReached(): Boolean {
        if (mAdapter.pics != null) {
            val pics = mAdapter.pics
            if (pics == null || pics.isEmpty()) {
                return false
            }
            val lastPic = pics.last()
            return xkcdListPresenter.lastItemReached(lastPic.num)
        }
        return false
    }

    override fun showScroller(visibility: Int) {
        rvScroller?.visibility = visibility
    }

    override fun updateData(pics: List<XkcdPic>) {
        mAdapter.updateData(pics)
    }

    override fun isLoadingMore(loadingMore: Boolean) {
        this.loadingMore = loadingMore
    }

    override fun onItemSelected(which: Int) {
        if (currentSelection.ordinal != which) {
            currentSelection = Selection.fromValue(which)
            reloadList(currentSelection)
            when (currentSelection) {
                ALL_COMICS -> logUXEvent(FIRE_FILTER_ALL)
                MY_FAVORITE -> logUXEvent(FIRE_FILTER_FAV)
                PEOPLES_CHOICE -> logUXEvent(FIRE_FILTER_THUMB)
            }
        }
    }

    internal enum class Selection(var id: Int) {
        ALL_COMICS(0),
        MY_FAVORITE(1),
        PEOPLES_CHOICE(2);
        
        companion object {
            fun fromValue(value: Int): Selection = values().find { it.id == value } ?: ALL_COMICS
        }
    }

    companion object {

        private const val COUNT_IN_ADV = 10

        private const val spanCount = 2
    }
}
