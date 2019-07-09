package xyz.jienan.xkcd.list.activity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.jienan.xkcd.Const.FIRE_WHAT_IF_SUFFIX
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.list.ListBaseAdapter
import xyz.jienan.xkcd.list.WhatIfListAdapter
import xyz.jienan.xkcd.list.contract.WhatIfListContract
import xyz.jienan.xkcd.list.presenter.WhatIfListPresenter
import xyz.jienan.xkcd.model.WhatIfArticle

/**
 * Created by jienanzhang on 22/03/2018.
 */

class WhatIfListActivity : BaseListActivity(), WhatIfListContract.View {

    override val mAdapter: ListBaseAdapter<out RecyclerView.ViewHolder> = WhatIfListAdapter()

    override val layoutManager = LinearLayoutManager(this)

    override val logSuffix = FIRE_WHAT_IF_SUFFIX

    override val presenter by lazy { WhatIfListPresenter(this) }

    override val filters = intArrayOf(R.string.filter_all_articles, R.string.filter_my_fav, R.string.filter_people_choice)

    override fun lastItemReached(): Boolean {
        if (!(mAdapter as WhatIfListAdapter).articles.isNullOrEmpty()) {
            val articles = mAdapter.articles!!
            val lastArticle = articles.last()
            return presenter.lastItemReached(lastArticle.num)
        }
        return false
    }

    override fun updateData(articles: List<WhatIfArticle>) {
        (mAdapter as WhatIfListAdapter).updateData(articles)
    }

    override fun getItemIndexOnPosition(position: Int) =
            (mAdapter as WhatIfListAdapter).getArticle(position)?.num?.toInt()
}
