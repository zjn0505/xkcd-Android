package xyz.jienan.xkcd.model.util

import android.text.TextUtils
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import xyz.jienan.xkcd.Const.URI_XKCD_EXPLAIN_EDIT
import java.io.IOException
import java.util.regex.Pattern

object XkcdExplainUtil {

    @Throws(IOException::class)
    fun getExplainFromHtml(responseBody: ResponseBody, url: String): String? {
        val doc = Jsoup.parse(responseBody.string())
        doc.setBaseUri(url)

        val h2Explain = doc.selectFirst("h2:has(span#Explanation)")

        val nextH2Index = h2Explain.siblingElements().select("h2").first().elementSiblingIndex()

        val explainElements = h2Explain.parent().children().subList(h2Explain.elementSiblingIndex() + 1, nextH2Index)

        explainElements.map { it.allElements }.forEach { it.cleanUp() }

        return Elements(explainElements).outerHtml()
    }

    fun isXkcdImageLink(url: String): Boolean {
        val regexExplain = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/\\d+(?!#)[:]?\\w+$"
        val regexXkcd = "^https?://(?:www\\.|m\\.)?xkcd\\.com/\\d+/?$"
        val extraXkcd = "^https?://(?:imgs\\.)?xkcd\\.com/comics/.*$"
        return url.matches(regexExplain.toRegex()) || url.matches(regexXkcd.toRegex()) || url.matches(extraXkcd.toRegex())
    }

    fun getXkcdIdFromExplainImageLink(url: String): Long {
        val regex = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/(\\d+).*$"
        val matcher = Pattern.compile(regex).matcher(url)
        return if (matcher.find()) {
            matcher.group(1)!!.toLong()
        } else {
            0
        }
    }

    fun getXkcdIdFromXkcdImageLink(url: String): Long {
        val regex = "^https?://(?:www\\.|m\\.)?xkcd\\.com/(\\d+)/?$"
        val matcher = Pattern.compile(regex).matcher(url)
        return if (matcher.find()) {
            matcher.group(1)!!.toLong()
        } else {
            -1
        }
    }

    private fun Elements.cleanUp() {
        map { it.allElements }.forEach { elements ->
            elements.select("h3 .editsection, h3 .mw-editsection, h4 .editsection, h4 .mw-editsection").remove()
            elements.select("p sup").filter { it.toString().contains("<i>citation needed</i>") }.map { it.remove() }
            elements.select("a[href]").forEach { it.refillToFullUrl() }
            elements.select("tbody").flatMap { it.children() }.map { it.append("<br />") }
        }
    }

    private fun Element.refillToFullUrl() {
        val href = attr("href")
        if (!TextUtils.isEmpty(href)) {
            if (href.startsWith("/wiki") || href.startsWith("#")) {
                attr("href", absUrl("href"))
            } else if (href.startsWith("//www.explainxkcd") && href.endsWith("action=edit")) {
                attr("href", URI_XKCD_EXPLAIN_EDIT)
            }
        }
    }
}
