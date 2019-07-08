package xyz.jienan.xkcd.list.activity

interface BaseListView {

    fun setLoading(isLoading: Boolean)

    fun showScroller(visibility: Int)
}