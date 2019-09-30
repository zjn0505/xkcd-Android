package xyz.jienan.xkcd.model.util

import org.jsoup.Jsoup

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import xyz.jienan.xkcd.base.network.NetworkService

object ExtraHtmlUtil {

    private const val XKCD_LINK = "http://www.xkcd.com/"

    fun getContentFromUrl(url: String): Observable<String> {
        return NetworkService.xkcdAPI.getExplain(url)
                .map { responseBody -> Jsoup.parse(responseBody.string()) }
                .map { doc ->
                    doc.head().appendCss("style.css")
                    val tableElement = doc.body().selectFirst("table[width=90%]")
                    tableElement?.removeAttr("width")
                    val imgElements = doc.body().select("img")
                    for (imgElement in imgElements) {
                        val src = imgElement.attr("src")
                        if (src != null && src.startsWith("http://imgs.xkcd")) {
                            imgElement.attr("src", src.replaceFirst("http:".toRegex(), "https:"))
                        }
                        if (XKCD_LINK == imgElement.parent().attr("href")) {
                            imgElement.parent()
                                    .removeAttr("href")
                                    .attr("href", imgElement.attr("src"))
                        }
                    }

                    doc
                }
                .map { it.outerHtml() }
                .subscribeOn(Schedulers.io())
    }
}