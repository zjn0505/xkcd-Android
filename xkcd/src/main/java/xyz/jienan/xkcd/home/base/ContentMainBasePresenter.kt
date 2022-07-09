package xyz.jienan.xkcd.home.base

import xyz.jienan.xkcd.base.BasePresenter

interface ContentMainBasePresenter : BasePresenter {
    fun favorited(currentIndex: Long, isFav: Boolean)

    fun liked(currentIndex: Long)

    fun setLastViewed(lastViewed: Int)

    fun getInfoAndShowFab(currentIndex: Int)

    var latest: Int

    fun getLastViewed(latestIndex: Int): Int

    fun loadLatest()

    fun searchContent(query: String)

    val randomUntouchedIndex: Long
}