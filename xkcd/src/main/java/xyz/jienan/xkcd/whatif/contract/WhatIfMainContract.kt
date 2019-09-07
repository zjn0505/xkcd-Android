package xyz.jienan.xkcd.whatif.contract

import xyz.jienan.xkcd.base.BaseView
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter
import xyz.jienan.xkcd.model.WhatIfArticle

interface WhatIfMainContract {

    interface View : BaseView<Presenter> {

        fun latestWhatIfLoaded(whatIfArticle: WhatIfArticle)

        fun showFab(whatIfArticle: WhatIfArticle)

        fun toggleFab(isFavorite: Boolean)

        fun showThumbUpCount(thumbCount: Long?)

        fun renderWhatIfSearch(articles: List<WhatIfArticle>)
    }

    interface Presenter : ContentMainBasePresenter {
        fun getBookmark() : Long

        fun setBookmark(index: Long) : Boolean
    }
}
