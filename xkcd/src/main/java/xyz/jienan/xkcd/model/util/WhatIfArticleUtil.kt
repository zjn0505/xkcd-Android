package xyz.jienan.xkcd.model.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

import okhttp3.ResponseBody
import xyz.jienan.xkcd.model.WhatIfArticle

object WhatIfArticleUtil {

    private const val BASE_URI = "https://what-if.xkcd.com/"

    private const val TEX_JS = "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/latest.js?config=TeX-MML-AM_CHTML"

    @Throws(IOException::class, ParseException::class)
    fun getArticlesFromArchive(responseBody: ResponseBody): MutableList<WhatIfArticle> {
        val doc = Jsoup.parse(responseBody.string(), BASE_URI)
        val divArchive = doc.select("div#archive-wrapper")
        val articles = mutableListOf<WhatIfArticle>()
        for (element in divArchive.first().children()) {


            val a = element.selectFirst("a[href]")
            val href = a.attr("href").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val archiveDate = element.selectFirst("h2.archive-date").html()
            val sdf = SimpleDateFormat("MMMMM d, yyyy", Locale.ENGLISH)

            val article = WhatIfArticle(num = href[href.size - 1].toLong(),
                    title = element.selectFirst("h1.archive-title").child(0).html(),
                    featureImg = a.child(0).absUrl("src"),
                    date = sdf.parse(archiveDate).time)

            articles.add(article)
        }
        return articles
    }

    @Throws(IOException::class)
    fun getArticleFromHtml(responseBody: ResponseBody): Document {
        val doc = Jsoup.parse(responseBody.string(), BASE_URI)
        val elements = doc.select("article.entry")
        elements.remove(elements[0].children().first())

        val imageElements = doc.select("img.illustration")
        for (element in imageElements) {
            element.attr("src", element.absUrl("src"))
            if (element.attr("src").startsWith("http://what-if.xkcd")) {
                element.attr("src", element.attr("src").replaceFirst("http:".toRegex(), "https:"))
            }
        }

        val pElements = doc.select("p")
        for (element in pElements) {
            if (element.html().split("\\[".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 1) {
                element.attr("class", "latex")
            }
        }

        val aElements = doc.select("a")
        for (element in aElements) {
            element.attr("href", element.absUrl("href"))
        }

        doc.head().html("")
        doc.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", "style.css")
        doc.head().appendElement("script")
                .attr("src", TEX_JS)
                .attr("async", "")
        doc.head().appendElement("script").attr("src", "LatexInterface.js")
        doc.head().appendElement("script").attr("src", "ImgInterface.js")
        doc.head().appendElement("script").attr("src", "RefInterface.js")
        doc.body().html(elements.html()).appendElement("p")
        return doc
    }
}
