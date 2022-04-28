package xyz.jienan.xkcd.list.presenter

import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import xyz.jienan.xkcd.list.contract.WhatIfListContract
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class WhatIfListPresenter(private val view: WhatIfListContract.View) : ListPresenter {

    private val compositeDisposable = CompositeDisposable()

    override fun loadList(startIndex: Int, reversed: Boolean) {
        view.setLoading(true)
        val data = WhatIfModel.loadArticlesFromDB()
        val dataSize = data!!.size
        if (dataSize == 0) {
            val d = WhatIfModel.loadAllWhatIf()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ articles -> updateView(articles, reversed) }
                    ) { e: Throwable? -> Timber.e(e, "update what if failed") }
            compositeDisposable.add(d)
        } else {
            updateView(data, reversed)
        }
    }

    override fun loadFavList() {
        view.updateData(WhatIfModel.favWhatIf)
        view.setLoading(false)
    }

    override fun loadPeopleChoiceList() {
        val latest = SharedPrefManager.latestWhatIf
        WhatIfModel.thumbUpList
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.setLoading(true) }
                .flatMapSingle { whatIfArticles ->
                    Observable.fromIterable(whatIfArticles)
                            .map { it.num }
                            .filter { num -> num <= latest }
                            .map { WhatIfModel.loadArticleFromDB(it)!! }
                            .toList()
                }
                .doOnNext { view.setLoading(false) }
                .subscribe({ view.updateData(it) },
                        { e -> Timber.e(e, "get top what if error") })
                .also { compositeDisposable.add(it) }
    }

    override fun hasFav(): Boolean {
        return WhatIfModel.favWhatIf.isNotEmpty()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    override fun lastItemReached(index: Long): Boolean {
        return index >= SharedPrefManager.latestWhatIf
    }

    private fun updateView(articles: List<WhatIfArticle>, reversed: Boolean) {
        view.showScroller(if (articles.isEmpty()) View.GONE else View.VISIBLE)
        view.updateData(if (reversed) articles.reversed() else articles)
        view.setLoading(false)
    }
}