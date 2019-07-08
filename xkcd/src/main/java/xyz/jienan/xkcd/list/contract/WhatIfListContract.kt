package xyz.jienan.xkcd.list.contract

import xyz.jienan.xkcd.base.BaseView
import xyz.jienan.xkcd.list.activity.BaseListView
import xyz.jienan.xkcd.list.presenter.ListPresenter
import xyz.jienan.xkcd.model.WhatIfArticle

interface WhatIfListContract {

    interface View : BaseView<ListPresenter>, BaseListView {
        fun updateData(articles: List<WhatIfArticle>)
    }
}
