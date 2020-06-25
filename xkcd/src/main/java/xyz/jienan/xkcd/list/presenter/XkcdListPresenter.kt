package xyz.jienan.xkcd.list.presenter

import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import xyz.jienan.xkcd.list.contract.XkcdListContract
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class XkcdListPresenter(private val view: XkcdListContract.View) : ListPresenter {

    private var inRequest = false

    private val compositeDisposable = CompositeDisposable()

    override fun loadList(startIndex: Int) {
        if (startIndex == 1) {
            view.setLoading(true)
        }

        val latestIndex = SharedPrefManager.latestXkcd

        if (latestIndex - BoxManager.allXkcd.size < 2) {
            updateView(latestIndex)
            return
        }

        val data = XkcdModel.loadXkcdFromDB(startIndex.toLong(), startIndex + 399.toLong())

        val dataSize = data.size
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", startIndex, dataSize)
        if (startIndex <= latestIndex - 399 && dataSize != 400 && startIndex != 401 ||
                startIndex == 401 && dataSize != 399 ||
                startIndex > latestIndex - 399 && startIndex + dataSize - 1.toLong() != latestIndex) {
            if (inRequest) {
                return
            }
            inRequest = true
            XkcdModel.loadRange(startIndex.toLong(), 400)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { list -> list[list.size - 1].num }
                    .doOnDispose { inRequest = false }
                    .singleOrError()
                    .subscribe({ lastIndex -> updateView(lastIndex) }
                    ) { Timber.e(it, "update xkcd failed") }
                    .also { compositeDisposable.add(it) }
        } else if (dataSize > 0) {
            updateView(data[dataSize - 1].num)
        }
    }

    override fun loadFavList() {
        view.updateData(XkcdModel.favXkcd)
        view.setLoading(false)
    }

    override fun loadPeopleChoiceList() {
        XkcdModel.thumbUpList
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.setLoading(true) }
                .doOnNext { view.setLoading(false) }
                .subscribe({ pics -> view.updateData(pics) }
                ) { Timber.e(it, "get top xkcd error") }
                .also { compositeDisposable.add(it) }
    }

    override fun hasFav(): Boolean {
        val list = XkcdModel.favXkcd
        if (list.isNotEmpty()) {
            XkcdModel.validateXkcdList(list)
                    .subscribe({ }) { Timber.e(it, "error on get pic info") }
                    .also { compositeDisposable.add(it) }
        }
        return list.isNotEmpty()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    override fun lastItemReached(index: Long) = index >= SharedPrefManager.latestXkcd

    private fun updateView(lastIndex: Long) {
        val xkcdPics = XkcdModel.loadXkcdFromDB(1, lastIndex)
        view.showScroller(if (xkcdPics.isEmpty()) View.GONE else View.VISIBLE)
        view.updateData(xkcdPics)
        view.isLoadingMore(false)
        view.setLoading(false)
    }

}