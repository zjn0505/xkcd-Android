package xyz.jienan.xkcd.comics.contract

import android.graphics.Bitmap

import xyz.jienan.xkcd.base.BasePresenter
import xyz.jienan.xkcd.base.BaseView
import xyz.jienan.xkcd.model.XkcdPic

interface ImageDetailPageContract {

    interface View : BaseView<Presenter> {

        fun setLoading(isLoading: Boolean)

        fun renderPic(url: String)

        fun renderTitle(xkcdPic: XkcdPic)

        fun renderSeekBar(duration: Int)

        fun renderFrame(bitmap: Bitmap)

        fun changeGifSeekBarProgress(progress: Int)

        fun showGifPlaySpeed(speed: Int)
    }

    interface Presenter : BasePresenter {

        fun requestImage(index: Int)

        fun parseGifData(data: ByteArray?)

        fun parseFrame(progress: Int)

        fun adjustGifSpeed(increaseByOne: Int)

        fun adjustGifFrame(isForward: Boolean)

        var isEcoMode: Boolean
    }
}
