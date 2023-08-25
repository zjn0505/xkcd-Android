package xyz.jienan.xkcd.model

import android.text.TextUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import xyz.jienan.xkcd.base.network.NetworkService
import xyz.jienan.xkcd.base.network.WHAT_IF_THUMBS_UP
import xyz.jienan.xkcd.base.network.WHAT_IF_TOP
import xyz.jienan.xkcd.base.network.XKCD_TOP_SORT_BY_THUMB_UP
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.util.WhatIfArticleUtil


object WhatIfModel {

    private val whatIfAPI = NetworkService.whatIfAPI

    private val picsPipeline = PublishSubject.create<WhatIfArticle>()

    private val zoomPipeline = PublishSubject.create<Int>()

    val favWhatIf: List<WhatIfArticle>
        get() = BoxManager.favWhatIf

    val thumbUpList: Single<List<WhatIfArticle>>
        get() = whatIfAPI.getTopWhatIfs(WHAT_IF_TOP, XKCD_TOP_SORT_BY_THUMB_UP)
                .subscribeOn(Schedulers.io())

    val untouchedList: List<WhatIfArticle>
        get() = BoxManager.untouchedArticleList

    fun push(article: WhatIfArticle) {
        picsPipeline.onNext(article)
    }

    fun setZoom(zoom: Int) {
        zoomPipeline.onNext(zoom)
    }

    fun observeZoom(): Observable<Int> {
        return zoomPipeline
    }

    fun observe(): Observable<WhatIfArticle> {
        return picsPipeline
    }

    fun loadAllWhatIf(): Single<List<WhatIfArticle>> {
        return whatIfAPI.archive
                .subscribeOn(Schedulers.io())
                .map { WhatIfArticleUtil.getArticlesFromArchive(it) }
                .map { BoxManager.updateAndSaveWhatIf(it.toMutableList()) }
    }

    fun loadLatest(): Single<WhatIfArticle> {
        return loadAllWhatIf()
                .map { it.last() }
    }

    fun loadArticle(id: Long): Single<WhatIfArticle> {
        return loadArticleContentFromDB(id)
                .switchIfEmpty(loadArticleFromAPI(id))
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadArticlesFromDB(): List<WhatIfArticle>? {
        return BoxManager.whatIfArchive
    }

    fun loadArticleFromDB(id: Long): WhatIfArticle? {
        return BoxManager.getWhatIf(id)
    }

    fun searchWhatIf(query: String, option: String): Single<MutableList<WhatIfArticle>> {
        return Observable.just(BoxManager.searchWhatIf(query, option))
                .singleOrError()
    }

    fun fav(index: Long, isFav: Boolean): Observable<WhatIfArticle> {
        return Observable.just(BoxManager.favWhatIf(index, isFav)!!)
    }

    /**
     * @param index
     * @return thumb up count
     */
    fun thumbsUp(index: Long): Single<Long> {
        return whatIfAPI.thumbsUpWhatIf(WHAT_IF_THUMBS_UP, index.toInt())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { BoxManager.likeWhatIf(index) }
                .map { it.thumbCount }
    }

    fun fastLoadWhatIfs(index: Long): Completable =
            Flowable.just(index)
                    .flatMap {
                        if (index == 0L) {
                            loadLatest().map { it.num }
                                    .toFlowable()
                                    .flatMap { Flowable.rangeLong(1, it) }
                        } else {
                            Flowable.rangeLong(1, index)
                        }
                    }.flatMapSingle {
                        if (loadArticleFromDB(it) == null || loadArticleFromDB(it)!!.content.isNullOrBlank()) {
                            loadArticleFromAPI(it)
                                .map { index }
                                .onErrorReturnItem(index)
                        } else {
                            Single.just(it)
                        }
                    }
                    .toList()
                    .ignoreElement()
                    .onErrorComplete()

    private fun loadArticleFromAPI(id: Long): Single<WhatIfArticle> {
        return whatIfAPI.getArticle(id)
                .subscribeOn(Schedulers.io())
                .map { WhatIfArticleUtil.getArticleFromHtml(it) }
                .map { it.html() }
                .map { BoxManager.updateAndSaveWhatIf(id, it) }
    }

    private fun loadArticleContentFromDB(id: Long): Maybe<WhatIfArticle> {
        val article = BoxManager.getWhatIf(id)
        return if (article == null || TextUtils.isEmpty(article.content)) Maybe.empty() else Maybe.just(article)
    }
}
