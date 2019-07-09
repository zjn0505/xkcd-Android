package xyz.jienan.xkcd.list.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_list.*
import xyz.jienan.xkcd.Const
import xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.list.ListBaseAdapter
import xyz.jienan.xkcd.list.ListFilterDialogFragment
import xyz.jienan.xkcd.list.presenter.ListPresenter
import xyz.jienan.xkcd.ui.RecyclerItemClickListener
import kotlin.math.abs

abstract class BaseListActivity : BaseActivity(), BaseListView, ListFilterDialogFragment.OnItemSelectListener {

    companion object {
        const val FLING_JUMP_LOW_THRESHOLD = 80

        const val FLING_JUMP_HIGH_THRESHOLD = 120

        private const val SELECTION = "Selection"
    }

    protected abstract val mAdapter: ListBaseAdapter<out RecyclerView.ViewHolder>

    protected abstract val layoutManager: RecyclerView.LayoutManager

    protected abstract val presenter: ListPresenter

    protected abstract val filters: IntArray

    private val rvScrollListener = object : RecyclerView.OnScrollListener() {

        private var dragging = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            dragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
            if (mAdapter.pauseLoading) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // user is touchy or the scroll finished, show images
                    mAdapter.pauseLoading = false
                } // settling means the user let the screen go, but it can still be flinging
            }

            if (!rvList.canScrollVertically(1) && lastItemReached() && newState == RecyclerView.SCROLL_STATE_IDLE) {
                logUXEvent(Const.FIRE_SCROLL_TO_END + logSuffix)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!dragging) {
                val currentSpeed = abs(dy)
                val paused = mAdapter.pauseLoading
                if (paused && currentSpeed < FLING_JUMP_LOW_THRESHOLD) {
                    mAdapter.pauseLoading = false
                } else if (!paused && FLING_JUMP_HIGH_THRESHOLD < currentSpeed) {
                    mAdapter.pauseLoading = true
                }
            }
            loadMoreCheck()
        }
    }

    protected open var currentSelection = Selection.ALL

    private fun onItemClick(position: Int) {
        if (position >= 0) {
            val intent = Intent()
            intent.putExtra(INTENT_TARGET_XKCD_ID, getItemIndexOnPosition(position))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    protected abstract fun getItemIndexOnPosition(position: Int): Int?

    protected abstract fun lastItemReached(): Boolean

    protected open val logSuffix = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        rvList.apply {
            rvScroller!!.setRecyclerView(this)
            adapter = mAdapter
            setHasFixedSize(true)

            addOnItemTouchListener(RecyclerItemClickListener(this, object : RecyclerItemClickListener.OnItemClickListener() {
                override fun onItemClick(view: View, position: Int) {
                    onItemClick(position)
                }
            }))
            layoutManager = this@BaseListActivity.layoutManager
            addOnScrollListener(rvScrollListener)
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            val selection = savedInstanceState.getInt(SELECTION, Selection.ALL.id)
            currentSelection = Selection.fromValue(selection)
        }
        reloadList(currentSelection)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentSelection != Selection.ALL) {
            outState.putInt(SELECTION, currentSelection.id)
        }
    }

    override fun onItemSelected(which: Int) {
        if (currentSelection.ordinal != which) {
            currentSelection = Selection.fromValue(which)
            reloadList(currentSelection)
            when (currentSelection) {
                Selection.ALL -> logUXEvent(Const.FIRE_FILTER_ALL + logSuffix)
                Selection.MY_FAVORITE -> logUXEvent(Const.FIRE_FILTER_FAV + logSuffix)
                Selection.PEOPLES_CHOICE -> logUXEvent(Const.FIRE_FILTER_THUMB + logSuffix)
            }
        }
    }

    open fun loadMoreCheck() {
        // no-ops
    }

    override fun showScroller(visibility: Int) {
        rvScroller!!.visibility = visibility
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (presenter.hasFav()) {
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
                filterDialog.setFilters(filters)
                if (!filterDialog.isAdded) {
                    filterDialog.show(supportFragmentManager, "filter")
                    filterDialog.setItemSelectListener(this)
                    filterDialog.setSelection(currentSelection.ordinal)
                    logUXEvent(Const.FIRE_LIST_FILTER_BAR + logSuffix)
                }
            }
        }
        return true
    }

    private fun reloadList(currentSelection: Selection) {
        when (currentSelection) {
            Selection.ALL -> presenter.loadList()
            Selection.MY_FAVORITE -> presenter.loadFavList()
            Selection.PEOPLES_CHOICE -> presenter.loadPeopleChoiceList()
        }
        rvList.scrollToPosition(0)
    }

    override fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbLoading?.visibility = View.VISIBLE
            rvList.visibility = View.GONE
        } else {
            pbLoading?.visibility = View.GONE
            rvList.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        rvList?.removeOnScrollListener(rvScrollListener)
        presenter.onDestroy()
        super.onDestroy()
    }

    protected enum class Selection(var id: Int) {
        ALL(0),
        MY_FAVORITE(1),
        PEOPLES_CHOICE(2);

        companion object {
            fun fromValue(value: Int): Selection = values().find { it.id == value } ?: ALL
        }
    }
}
