package xyz.jienan.xkcd.model.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat

import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity

import xyz.jienan.xkcd.Const.URI_XKCD_EXPLAIN_EDIT

object ExplainLinkUtil {

    fun setTextViewHTML(text: TextView, html: String) {
        val sequence = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        urls.forEach { strBuilder.makeLinkClickable(text.context, it) }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun SpannableStringBuilder.makeLinkClickable(context: Context, span: URLSpan) {
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        val flags = getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                val url = span.url
                if (XkcdExplainUtil.isXkcdImageLink(url)) {
                    val id = XkcdExplainUtil.getXkcdIdFromExplainImageLink(url)
                    ImageDetailPageActivity.startActivityFromId(context, id)
                } else if (URLUtil.isNetworkUrl(url)) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    if (browserIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(browserIntent)
                    }
                } else if (URI_XKCD_EXPLAIN_EDIT == url) {
                    Toast.makeText(context, R.string.uri_hint_explain_edit, Toast.LENGTH_SHORT).show()
                }
                if (BuildConfig.DEBUG) {
                    Toast.makeText(context, url, Toast.LENGTH_SHORT).show()
                }
            }
        }
        setSpan(clickable, start, end, flags)
        removeSpan(span)
    }
}
