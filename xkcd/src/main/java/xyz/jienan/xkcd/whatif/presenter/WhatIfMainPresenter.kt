package xyz.jienan.xkcd.whatif.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import timber.log.Timber
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract
import kotlin.random.Random

class WhatIfMainPresenter constructor(private val view: WhatIfMainContract.View) : WhatIfMainContract.Presenter {

    private val compositeDisposable = CompositeDisposable()

    private var fabShowDisposable = Disposables.empty()

    private var searchDisposable = Disposables.empty()

    override fun favorited(index: Long, isFav: Boolean) {
        if (index < 1) {
            return
        }
        WhatIfModel.fav(index, isFav).subscribe({},
                { e -> Timber.e(e, "error on get one pic: %d", index) })
                .also { compositeDisposable.add(it) }
        view.toggleFab(isFav)
    }

    override fun liked(currentIndex: Long) {
        if (currentIndex < 1) {
            return
        }

        WhatIfModel
                .thumbsUp(currentIndex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.showThumbUpCount(it) },
                        { e -> Timber.e(e, "Thumbs up failed") })
                .also { compositeDisposable.add(it) }
    }

    override fun getInfoAndShowFab(index: Int) {
        if (!fabShowDisposable.isDisposed) {
            fabShowDisposable.dispose()
        }
        val article = WhatIfModel.loadArticleFromDB(index.toLong())
        if (article == null) {
            fabShowDisposable = WhatIfModel.observe()
                    .filter { (num) -> num == index.toLong() }
                    .subscribe({ view.showFab(it) },
                            { e -> Timber.e(e, "what if pipeline observing error") })
            compositeDisposable.add(fabShowDisposable)
        } else {
            view.showFab(article)
        }
    }

    override fun setLastViewed(lastViewed: Int) {
        SharedPrefManager.setLastViewedWhatIf(lastViewed.toLong())
    }

    override fun getLatest() = SharedPrefManager.latestWhatIf.toInt()

    override fun setLatest(latestIndex: Int) {
        SharedPrefManager.setLastViewedWhatIf(latestIndex.toLong())
    }

    override fun getLastViewed(latestIndex: Int) =
            SharedPrefManager.getLastViewedWhatIf(latestIndex.toLong()).toInt()

    override fun loadLatest() {
        WhatIfModel.loadLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ whatIfArticle ->
                    val latestIndex = whatIfArticle.num
                    SharedPrefManager.latestWhatIf = latestIndex
                    view.latestWhatIfLoaded(whatIfArticle)
                }, { e -> Timber.e(e, "load what if article error") })
                .also { compositeDisposable.add(it) }
    }

    override fun searchContent(query: String) {

        if (!searchDisposable.isDisposed) {
            searchDisposable.dispose()
        }

        searchDisposable = WhatIfModel.searchWhatIf(query, SharedPrefManager.whatIfSearchPref)
                .map { list ->
                    if (isNumQuery(query)) {
                        val num = query.toLong()

                        val matchNumArticle = list.firstOrNull { it.num == num }

                        if (matchNumArticle != null) {
                            list.remove(matchNumArticle)
                            list.add(0, matchNumArticle)
                        }
                    }
                    list
                }
                .subscribe({ view.renderWhatIfSearch(it) },
                        { e ->
                            Timber.e(e, "search what if error")
                            if (isNumQuery(query)) {
                                val num = java.lang.Long.parseLong(query)
                                val article = WhatIfModel.loadArticleFromDB(num)
                                if (article != null) {
                                    view.renderWhatIfSearch(listOf(article))
                                }
                            }
                        })
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        fabShowDisposable.dispose()
        searchDisposable.dispose()
    }

    override fun getRandomUntouchedIndex(): Long {
        val list = WhatIfModel.untouchedList
        return if (list.isEmpty()) {
            0
        } else {
            list[Random.nextInt(list.size)].num
        }
    }

    private fun isNumQuery(query: String) =
            try {
                val num = query.toLong()
                num > 0 && num <= SharedPrefManager.latestWhatIf
            } catch (e: NumberFormatException) {
                false
            }
}
