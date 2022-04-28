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

    override fun loadList(startIndex: Int, reversed: Boolean) {
        if (startIndex == 1) {
            view.setLoading(true)
        }

        val latestIndex = SharedPrefManager.latestXkcd

        if (latestIndex - BoxManager.allXkcd.size < 2) {
            updateView(latestIndex, reversed)
            return
        }
        var realStartIndex = startIndex
        val data = if (reversed) {
            if (startIndex == 1) {
                realStartIndex = latestIndex.toInt()
            }
            XkcdModel.loadXkcdFromDB(realStartIndex - 399L, realStartIndex.toLong())
        } else {
            XkcdModel.loadXkcdFromDB(realStartIndex.toLong(), realStartIndex + 399L)
        }

        val dataSize = data.size
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", realStartIndex, dataSize)
        if (realStartIndex <= latestIndex - 399 && dataSize != 400 && realStartIndex != 401 ||
                !reversed && realStartIndex == 401 && dataSize != 399 ||
                !reversed && realStartIndex > latestIndex - 399 && realStartIndex + dataSize - 1.toLong() != latestIndex) {
            if (inRequest) {
                return
            }
            inRequest = true
            XkcdModel.loadRange(realStartIndex.toLong(), 400, reversed = if (reversed) 1 else 0)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { list -> list[list.size - 1].num }
                    .doOnDispose { inRequest = false }
                    .singleOrError()
                    .subscribe({ lastIndex -> updateView(lastIndex, reversed) }
                    ) { Timber.e(it, "update xkcd failed") }
                    .also { compositeDisposable.add(it) }
        } else if (dataSize > 0) {
            updateView(data[dataSize - 1].num, reversed)
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

    private fun updateView(lastIndex: Long, reversed: Boolean) {
        val xkcdPics = XkcdModel.loadXkcdFromDB(1, lastIndex)
        view.showScroller(if (xkcdPics.isEmpty()) View.GONE else View.VISIBLE)
        view.updateData(if (reversed) xkcdPics.reversed() else xkcdPics)
        view.isLoadingMore(false)
        view.setLoading(false)
    }

}