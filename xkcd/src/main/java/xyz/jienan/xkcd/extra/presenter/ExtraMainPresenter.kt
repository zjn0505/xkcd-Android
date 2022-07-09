package xyz.jienan.xkcd.extra.presenter

import io.objectbox.reactive.DataSubscription
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import xyz.jienan.xkcd.extra.contract.ExtraMainContract
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.ExtraModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import java.util.*

class ExtraMainPresenter(private val view: ExtraMainContract.View) : ExtraMainContract.Presenter {

    private val sharedPrefManager = SharedPrefManager

    private val compositeDisposable = CompositeDisposable()

    private var extraComics: List<ExtraComics> = ArrayList()

    private var subscription: DataSubscription? = null

    override fun loadLatest() {
        extraComics = ExtraModel.all
        view.showExtras(extraComics)
    }

    override fun observe() {
        subscription?.cancel()
        subscription = ExtraModel.observe
                .observer {
                    if (it.size != extraComics.size && it.isNotEmpty()) {
                        Timber.d("Show extra $it")
                        extraComics = it
                        view.showExtras(extraComics)
                        subscription?.cancel()
                    }
                }
    }

    override fun dispose() {
        subscription?.cancel()
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

    override var latest: Int
        get() = extraComics.size
        set(value) {
            // no ops
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

    override val randomUntouchedIndex: Long
        get() = 0L
}
