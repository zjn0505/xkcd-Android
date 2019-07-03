package xyz.jienan.xkcd.model


import android.text.TextUtils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.jienan.xkcd.base.network.NetworkService
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.util.ExtraHtmlUtil
import xyz.jienan.xkcd.model.util.XkcdExplainUtil

object ExtraModel {

    val all: List<ExtraComics>
        get() = BoxManager.extraList

    fun getExtra(index: Int): ExtraComics = BoxManager.getExtra(index)

    fun update(extraComics: List<ExtraComics>) {
        BoxManager.saveExtras(extraComics)
    }

    fun loadExplain(url: String): Observable<String> {
        val explainFromDB = BoxManager.loadExtraExplain(url)
        return if (!TextUtils.isEmpty(explainFromDB)) {
            Observable.just(explainFromDB)
        } else NetworkService.xkcdAPI.getExplain(url).subscribeOn(Schedulers.io())
                .map { responseBody -> XkcdExplainUtil.getExplainFromHtml(responseBody, url) }

    }

    fun saveExtraWithExplain(url: String, explainContent: String) {
        BoxManager.updateExtra(url, explainContent)
    }

    fun parseContentFromUrl(url: String): Observable<String> {
        return ExtraHtmlUtil.getContentFromUrl(url)
                .observeOn(AndroidSchedulers.mainThread())
    }
}
