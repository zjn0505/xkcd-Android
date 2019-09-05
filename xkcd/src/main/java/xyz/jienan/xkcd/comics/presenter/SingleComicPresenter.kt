package xyz.jienan.xkcd.comics.presenter

import android.graphics.Bitmap

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import xyz.jienan.xkcd.comics.contract.SingleComicContract
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class SingleComicPresenter(private val view: SingleComicContract.View) : SingleComicContract.Presenter {

    private val compositeDisposable = CompositeDisposable()

    override fun getExplain(index: Long) {
        val latestIndex = SharedPrefManager.latestXkcd

        val d = XkcdModel.loadExplain(index, latestIndex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.explainLoaded(it) }, { e ->
                    view.explainFailed()
                    Timber.e(e, "Load explainUrl failed")
                })
        compositeDisposable.add(d)
    }

    override fun loadXkcd(index: Int) {
        val latestIndex = SharedPrefManager.latestXkcd
        val xkcdPicInDB = XkcdModel.loadXkcdFromDB(index.toLong())
        val shouldQueryNetwork = latestIndex - index < 10
        view.setLoading(true)

        if (shouldQueryNetwork || xkcdPicInDB == null) {
            XkcdModel.loadXkcd(index.toLong())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { view.setLoading(false) }
                    .filter { xkcdPicInDB == null }
                    .subscribe( { this.renderComic(it) },
                            { e -> Timber.e(e, "load xkcd pic error") })
                    .also { compositeDisposable.add(it) }
        }

        if (xkcdPicInDB != null) {
            renderComic(xkcdPicInDB)
        }
    }

    override fun updateXkcdSize(xkcdPic: XkcdPic?, resource: Bitmap?) {
        if (xkcdPic != null && (xkcdPic.width == 0 || xkcdPic.height == 0) && resource != null) {
            XkcdModel.updateSize(xkcdPic.num, resource.width, resource.height)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    private fun renderComic(xkcdPic: XkcdPic) {
        view.renderXkcdPic(xkcdPic)
        XkcdModel.push(xkcdPic)
    }
}
