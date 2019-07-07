package xyz.jienan.xkcd.list.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import kotlinx.android.synthetic.main.activity_list.*
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.list.ListFilterDialogFragment
import xyz.jienan.xkcd.list.WhatIfListAdapter
import xyz.jienan.xkcd.list.activity.WhatIfListActivity.Selection.*
import xyz.jienan.xkcd.list.contract.WhatIfListContract
import xyz.jienan.xkcd.list.presenter.WhatIfListPresenter
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.ui.RecyclerItemClickListener

/**
 * Created by jienanzhang on 22/03/2018.
 */

class WhatIfListActivity : BaseActivity(), WhatIfListContract.View, ListFilterDialogFragment.OnItemSelectListener {

    private val mAdapter by lazy { WhatIfListAdapter() }

    private val linearLayoutManager by lazy { LinearLayoutManager(this) }

    private var currentSelection = ALL_WHAT_IF

    private val whatIfListPresenter by lazy { WhatIfListPresenter(this) }

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
                logUXEvent(FIRE_SCROLL_TO_END + FIRE_WHAT_IF_SUFFIX)
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
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentSelection != ALL_WHAT_IF) {
            outState.putInt("Selection", currentSelection.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (whatIfListPresenter.hasFav()) {
            menuInflater.inflate(R.menu.menu_list, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_filter -> {
                val filterDialog = supportFragmentManager.findFragmentByTag("filter")
                        as ListFilterDialogFragment? ?: ListFilterDialogFragment()
                val filters = intArrayOf(R.string.filter_all_articles, R.string.filter_my_fav, R.string.filter_people_choice)
                filterDialog.setFilters(filters)
                if (!filterDialog.isAdded) {
                    filterDialog.show(supportFragmentManager, "filter")
                    filterDialog.setItemSelectListener(this)
                    filterDialog.setSelection(currentSelection.ordinal)
                    logUXEvent(FIRE_LIST_FILTER_BAR + FIRE_WHAT_IF_SUFFIX)
                }
            }
        }
        return true
    }

    private fun reloadList(currentSelection: Selection) {
        when (currentSelection) {
            ALL_WHAT_IF -> whatIfListPresenter.loadList()
            MY_FAVORITE -> whatIfListPresenter.loadFavList()
            PEOPLES_CHOICE -> whatIfListPresenter.loadPeopleChoiceList()
        }
        rvList?.scrollToPosition(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        rvScroller?.setRecyclerView(rvList)
        rvList?.adapter = mAdapter
        rvList?.setHasFixedSize(true)
        rvList?.addOnItemTouchListener(RecyclerItemClickListener(this, rvList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (position >= 0) {
                    val intent = Intent()
                    intent.putExtra(INTENT_TARGET_XKCD_ID, mAdapter.getArticle(position)?.num?.toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            override fun onLongItemClick(view: View, position: Int) {
                // no-ops
            }
        }))
        rvList!!.layoutManager = linearLayoutManager
        rvList!!.addOnScrollListener(rvScrollListener)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            val selection = savedInstanceState.getInt("Selection", ALL_WHAT_IF.id)
            currentSelection = Selection.fromValue(selection)
        } else {
            sharedPreferences.edit().putInt("FILTER_SELECTION", ALL_WHAT_IF.id).apply()
        }
        reloadList(currentSelection)
    }

    override fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbLoading!!.visibility = View.VISIBLE
            rvList!!.visibility = View.GONE
        } else {
            pbLoading!!.visibility = View.GONE
            rvList!!.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        rvList!!.removeOnScrollListener(rvScrollListener)
        whatIfListPresenter.onDestroy()
        super.onDestroy()
    }

    private fun lastItemReached(): Boolean {
        if (mAdapter.articles != null) {
            val articles = mAdapter.articles
            if (articles == null || articles.isEmpty()) {
                return false
            }
            val lastArticle = articles.last()
            return whatIfListPresenter.lastItemReached(lastArticle.num)
        }
        return false
    }

    override fun showScroller(visibility: Int) {
        rvScroller!!.visibility = visibility
    }

    override fun updateData(articles: List<WhatIfArticle>) {
        mAdapter.updateData(articles)
    }

    override fun onItemSelected(which: Int) {
        if (currentSelection.ordinal != which) {
            currentSelection = Selection.fromValue(which)
            reloadList(currentSelection)
            when (currentSelection) {
                ALL_WHAT_IF -> logUXEvent(FIRE_FILTER_ALL + FIRE_WHAT_IF_SUFFIX)
                MY_FAVORITE -> logUXEvent(FIRE_FILTER_FAV + FIRE_WHAT_IF_SUFFIX)
                PEOPLES_CHOICE -> logUXEvent(FIRE_FILTER_THUMB + FIRE_WHAT_IF_SUFFIX)
            }
        }
    }

    internal enum class Selection(var id: Int) {
        ALL_WHAT_IF(0),
        MY_FAVORITE(1),
        PEOPLES_CHOICE(2);

        companion object {
            fun fromValue(value: Int): Selection = values().find { it.id == value } ?: ALL_WHAT_IF
        }
    }
}
