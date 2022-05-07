package xyz.jienan.xkcd.model.util

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber
import xyz.jienan.xkcd.model.WhatIfArticle
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object WhatIfArticleUtil {

    private const val BASE_URI = "https://what-if.xkcd.com/"

    private const val TEX_JS = "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/latest.js?config=TeX-MML-AM_CHTML"

    @Throws(IOException::class, ParseException::class)
    fun getArticlesFromArchive(responseBody: ResponseBody): List<WhatIfArticle> {
        val doc = Jsoup.parse(responseBody.string(), BASE_URI)
        val articleDivs = doc.select("div.archive-entry")

        Timber.d("Article divs size ${articleDivs.size}")
        return articleDivs.map { it.getWhatIfArticleFromArchiveEntry() }
    }

    @Throws(IOException::class)
    fun getArticleFromHtml(responseBody: ResponseBody): Document {
        val doc = Jsoup.parse(responseBody.string(), BASE_URI)

        doc.head().appendScriptsToHead()

        doc.selectFirst("article#entry").apply {
            cleanUp()
            appendElement("p")
            doc.body().html(html())
        }

        return doc
    }

    private fun Element.cleanUp() {
        select("img.illustration").forEach { it.convertToFullHttpsImgUrl() }
        select("p").filter { it.html().split("\\[").size > 1 }.forEach { it.tagLatex() }
        select("a").map { it.attr("href", it.absUrl("href")) }
    }

    private fun Element.convertToFullHttpsImgUrl() {
        attr("src", absUrl("src").replaceFirst("^http://what".toRegex(), "https://what"))
    }

    private fun Element.tagLatex() {
        when {
            html().startsWith("\\[") -> attr("class", "latex")
            parent().html().contains("<a href=\"//what-if.xkcd.com/124/\">") -> {
                val contents = html()
                contents.replace("\\(", "\\[")
                contents.replace("\\)", "\\]")
                html(contents).appendElement("br")
            }
            else -> {
                val taggedLatex = html().replace("(?=\\\\\\[)".toRegex(), "<p class=\"latex\">")
                        .replace("(?<=\\\\\\])".toRegex(), "<p>")
                html(taggedLatex)
            }
        }
    }

    private fun Element.appendScriptsToHead() {
        empty()
        appendCss("style.css")
        appendElement("script")
                .attr("src", TEX_JS)
                .attr("async", "")
        appendElement("script").attr("src", "LatexInterface.js")
        appendElement("script").attr("src", "ImgInterface.js")
        appendElement("script").attr("src", "RefInterface.js")
    }

    private fun Element.getWhatIfArticleFromArchiveEntry(): WhatIfArticle {
        val a = selectFirst("a[href]")
        val href = a.attr("href").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val archiveDate = selectFirst("h3.archive-date").html()
        val sdf = SimpleDateFormat("MMMMM d, yyyy", Locale.ENGLISH)
        val title = selectFirst("h2.archive-title a").html()
        val num = href.last().toLong()
        val featureImg = a.child(0).absUrl("src")

        return WhatIfArticle(num = num,
                title = title,
                featureImg = featureImg,
                date = sdf.parse(archiveDate).time)
    }
}

fun Element.appendCss(cssName: String) {
    appendElement("link")
            .attr("rel", "stylesheet")
            .attr("type", "text/css")
            .attr("href", cssName)
}
