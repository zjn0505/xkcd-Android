package xyz.jienan.xkcd.list.activity

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.list.ListBaseAdapter
import xyz.jienan.xkcd.list.XkcdListGridAdapter
import xyz.jienan.xkcd.list.contract.XkcdListContract
import xyz.jienan.xkcd.list.presenter.XkcdListPresenter
import xyz.jienan.xkcd.model.XkcdPic

/**
 * Created by jienanzhang on 22/03/2018.
 */

class XkcdListActivity : BaseListActivity(), XkcdListContract.View {

    override val mAdapter: ListBaseAdapter<out RecyclerView.ViewHolder> = XkcdListGridAdapter()

    override val layoutManager = StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL)

    private var loadingMore = false

    override val presenter by lazy { XkcdListPresenter(this) }

    override val filters= intArrayOf(R.string.filter_all_comics, R.string.filter_my_fav, R.string.filter_people_choice)

    override fun lastItemReached(): Boolean {
        if (!(mAdapter as XkcdListGridAdapter).pics.isNullOrEmpty()) {
            val pics = mAdapter.pics!!
            val lastPic = pics.last()
            return presenter.lastItemReached(lastPic.num)
        }
        return false
    }

    override fun updateData(pics: List<XkcdPic>) {
        (mAdapter as XkcdListGridAdapter).updateData(pics)
    }

    override fun isLoadingMore(loadingMore: Boolean) {
        this.loadingMore = loadingMore
    }

    override fun getItemIndexOnPosition(position: Int) =
        (mAdapter as XkcdListGridAdapter).getPic(position)?.num?.toInt()

    override fun loadMoreCheck() {
        if (currentSelection != Selection.ALL) {
            return
        }
        val visibleItemCount = layoutManager.childCount
        var firstVisibleItemPositions = IntArray(SPAN_COUNT)
        firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(firstVisibleItemPositions)
        if (firstVisibleItemPositions[1] + visibleItemCount >= (mAdapter as XkcdListGridAdapter).itemCount - COUNT_IN_ADV
                && !loadingMore
                && !lastItemReached()) {
            loadingMore = true
            presenter.loadList((mAdapter.pics!![mAdapter.itemCount - 1].num + 1).toInt())
        }
    }

    companion object {

        private const val COUNT_IN_ADV = 10

        private const val SPAN_COUNT = 2
    }
}
