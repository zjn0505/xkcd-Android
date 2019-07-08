package xyz.jienan.xkcd.comics.presenter

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import timber.log.Timber
import xyz.jienan.xkcd.comics.contract.ComicsMainContract
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import java.util.*
import java.util.concurrent.TimeUnit

class ComicsMainPresenter(private val view: ComicsMainContract.View) : ComicsMainContract.Presenter {

    private val compositeDisposable = CompositeDisposable()

    private var fabShowDisposable = Disposables.empty()

    private var searchDisposable = Disposables.empty()

    override fun loadLatest() {
        XkcdModel.loadLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ xkcdPic ->
                    SharedPrefManager.latestXkcd = xkcdPic.num
                    view.latestXkcdLoaded(xkcdPic)
                }, { e -> Timber.e(e, "load xkcd pic error") })
                .also { compositeDisposable.add(it) }
    }

    override fun liked(index: Long) {
        if (index < 1) {
            return
        }
        XkcdModel.thumbsUp(index)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.showThumbUpCount(it) },
                        { e -> Timber.e(e, "Thumbs up failed") })
                .also { compositeDisposable.add(it) }
    }

    override fun favorited(index: Long, isFav: Boolean) {
        if (index < 1) {
            return
        }
        XkcdModel.fav(index, isFav).subscribe({ },
                { e -> Timber.e(e, "error on get one pic: %d", index) })
                .also { compositeDisposable.add(it) }
        view.toggleFab(isFav)
    }

    override fun fastLoad(latestIndex: Int) {
        if (latestIndex <= 0) {
            return
        }
        Timber.d("Start fast load")
        XkcdModel.fastLoad(latestIndex)
                .subscribe({ Timber.d("Fast load succeed") },
                        { e -> Timber.e(e, "Error in fast load") })
                .also { compositeDisposable.add(it) }
    }

    override fun getInfoAndShowFab(index: Int) {
        if (!fabShowDisposable.isDisposed) {
            fabShowDisposable.dispose()
        }
        val xkcdPic = XkcdModel.loadXkcdFromDB(index.toLong())
        if (xkcdPic == null) {
            fabShowDisposable = XkcdModel.observe()
                    .filter { (_, _, _, num) -> num == index.toLong() }
                    .subscribe({ view.showFab(it) },
                            { Timber.e(it, "pic pipeline observing error") })
            compositeDisposable.add(fabShowDisposable)
        } else {
            view.showFab(xkcdPic)
        }
    }

    override fun getLatest() =
            SharedPrefManager.latestXkcd.toInt()

    override fun setLatest(latestIndex: Int) {
        SharedPrefManager.latestXkcd = latestIndex.toLong()
    }

    override fun setLastViewed(lastViewed: Int) {
        SharedPrefManager.setLastViewedXkcd(lastViewed)
    }

    override fun getLastViewed(latestIndex: Int) =
            SharedPrefManager.getLastViewedXkcd(latestIndex).toInt()

    override fun onDestroy() {
        compositeDisposable.dispose()
        fabShowDisposable.dispose()
        searchDisposable.dispose()
    }


    override fun searchContent(query: String) {

        if (!searchDisposable.isDisposed) {
            searchDisposable.dispose()
        }

        val isNumQuery = isNumQuery(query)

        val numPic = if (isNumQuery) XkcdModel.loadXkcdFromDB(query.toLong()) else null

        searchDisposable = XkcdModel.search(query)
                .startWith(if (numPic != null) {
                    Observable.just(listOf(numPic))
                } else {
                    Observable.empty()
                }).debounce(200, TimeUnit.MILLISECONDS)
                .map { list -> list.toMutableList() }
                .map { list ->
                    if (isNumQuery) {
                        val num = query.toLong()
                        moveNumberQueryToFirstPlace(list, num)
                    }
                    list
                }
                .filter { xkcdPics -> !xkcdPics.isNullOrEmpty() }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.renderXkcdSearch(it) },
                        { e ->
                            Timber.e(e, "search error")
                            if (numPic != null) {
                                view.renderXkcdSearch(listOf(numPic))
                            }
                        })
        compositeDisposable.add(searchDisposable)
    }

    override fun getRandomUntouchedIndex(): Long {
        val list = XkcdModel.untouchedList
        return if (list.isEmpty()) {
            0
        } else {
            list[Random().nextInt(list.size)].num
        }
    }

    private fun moveNumberQueryToFirstPlace(list: MutableList<XkcdPic>, num: Long) {
        val matchNumXkcd = list.firstOrNull { it.num == num }

        if (matchNumXkcd != null) {
            list.remove(matchNumXkcd)
            list.add(0, matchNumXkcd)
        }
    }

    private fun isNumQuery(query: String) =
            try {
                val num = query.toLong()
                num > 0 && num <= SharedPrefManager.latestXkcd
            } catch (e: NumberFormatException) {
                false
            }
}
