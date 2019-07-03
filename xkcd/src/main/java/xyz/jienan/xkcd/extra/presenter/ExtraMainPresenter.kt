package xyz.jienan.xkcd.extra.presenter

import java.util.ArrayList

import io.reactivex.disposables.CompositeDisposable
import xyz.jienan.xkcd.extra.contract.ExtraMainContract
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.ExtraModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class ExtraMainPresenter(private val view: ExtraMainContract.View) : ExtraMainContract.Presenter {

    private val sharedPrefManager = SharedPrefManager

    private val compositeDisposable = CompositeDisposable()

    private var extraComics: List<ExtraComics> = ArrayList()

    override fun loadLatest() {
        extraComics = ExtraModel.all
        view.showExtras(extraComics)
    }

    override fun liked(index: Long) {
        // no-ops
    }

    override fun favorited(index: Long, isFav: Boolean) {
        // no-ops
    }

    override fun getInfoAndShowFab(index: Int) {
        // no-ops
    }

    override fun getLatest() = extraComics.size

    override fun setLatest(latestIndex: Int) {
        // no-ops
    }

    override fun setLastViewed(lastViewed: Int) {
        sharedPrefManager.setLastViewedExtra(lastViewed)
    }

    override fun getLastViewed(latestIndex: Int) = sharedPrefManager.getLastViewedExtra(latestIndex)

    override fun onDestroy() {
        compositeDisposable.dispose()
    }


    override fun searchContent(query: String) {
        // no-ops
    }

    override fun getRandomUntouchedIndex() = 0L
}
