package xyz.jienan.xkcd.list.presenter

import xyz.jienan.xkcd.base.BasePresenter

interface ListPresenter : BasePresenter {

    fun loadList(startIndex: Int = 1)

    fun hasFav(): Boolean

    fun loadFavList()

    fun loadPeopleChoiceList()

    fun lastItemReached(index: Long): Boolean
}