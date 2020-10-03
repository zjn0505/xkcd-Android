package xyz.jienan.xkcd.comics.contract

import android.graphics.Bitmap

import xyz.jienan.xkcd.base.BasePresenter
import xyz.jienan.xkcd.base.BaseView
import xyz.jienan.xkcd.model.XkcdPic

interface SingleComicContract {

    interface View : BaseView<Presenter> {

        var translationMode : Int

        fun explainLoaded(result: String)

        fun explainFailed()

        fun renderXkcdPic(xkcdPic: XkcdPic)

        fun setLoading(isLoading: Boolean)

        fun setAltTextVisibility(gone: Boolean)
    }

    interface Presenter : BasePresenter {

        fun loadXkcd(index: Int)

        fun getExplain(index: Long)

        fun updateXkcdSize(xkcdPic: XkcdPic?, resource: Bitmap?)

        val showLocalXkcd : Boolean
    }
}
