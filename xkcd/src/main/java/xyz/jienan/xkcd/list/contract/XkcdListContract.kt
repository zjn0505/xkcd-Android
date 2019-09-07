package xyz.jienan.xkcd.list.contract

import xyz.jienan.xkcd.base.BaseView
import xyz.jienan.xkcd.list.activity.BaseListView
import xyz.jienan.xkcd.list.presenter.ListPresenter
import xyz.jienan.xkcd.model.XkcdPic

interface XkcdListContract {

    interface View : BaseView<ListPresenter>, BaseListView {

        fun updateData(pics: List<XkcdPic>)

        fun isLoadingMore(isLoadingMore: Boolean)
    }
}
