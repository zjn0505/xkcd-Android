package xyz.jienan.xkcd.comics.presenter

import com.google.gson.internal.bind.util.ISO8601Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import timber.log.Timber
import xyz.jienan.xkcd.Const.XKCD_BOOKMARK
import xyz.jienan.xkcd.comics.contract.ComicsMainContract
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import java.text.ParsePosition
import java.util.*
import java.util.concurrent.TimeUnit

class ComicsMainPresenter(private val view: ComicsMainContract.View) : ComicsMainContract.Presenter {

    private val compositeDisposable = CompositeDisposable()

    private var fabShowDisposable = Disposables.empty()

    private var searchDisposable = Disposables.empty()

    private val iso8601 = "(?:20)[0-9]{2}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-9])|(?:(?!02)(?:0[1-9]|1[0-2])-(?:30))|(?:(?:0[13578]|1[02])-31))".toRegex()

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

    override var latest: Int
        get() = SharedPrefManager.latestXkcd.toInt()
        set(value) {
            SharedPrefManager.latestXkcd = value.toLong()
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

        if (iso8601.matches(query)) {
            val date = ISO8601Utils.parse(query, ParsePosition(0))
            val result = BoxManager.searchXkcdByDate(Triple(date.year + 1900, date.month + 1, date.date))
            if (result != null) {
                view.renderXkcdSearch(listOf(result))
                return
            }
        }

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

    override val randomUntouchedIndex: Long
        get() {
            val list = XkcdModel.untouchedList
            return if (list.isEmpty()) {
                0
            } else {
                list[Random().nextInt(list.size)].num
            }
        }

    override fun getBookmark() = SharedPrefManager.getBookmark(XKCD_BOOKMARK)

    override fun setBookmark(index: Long): Boolean {
        return if (index > 0) {
            SharedPrefManager.setBookmark(XKCD_BOOKMARK, index)
            true
        } else {
            false
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
