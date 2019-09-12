package xyz.jienan.xkcd.model.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;

import static xyz.jienan.xkcd.Const.URI_XKCD_EXPLAIN_EDIT;

public class ExplainLinkUtil {

    public static void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(text.getContext(), strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void makeLinkClickable(Context context, SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(@NonNull View view) {
                String url = span.getURL();
                if (XkcdExplainUtil.INSTANCE.isXkcdImageLink(url)) {
                    final long id = XkcdExplainUtil.INSTANCE.getXkcdIdFromExplainImageLink(url);
                    ImageDetailPageActivity.startActivityFromId(context, id);
                } else if (URLUtil.isNetworkUrl(url)) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(browserIntent);
                    }
                } else if (URI_XKCD_EXPLAIN_EDIT.equals(url)) {
                    Toast.makeText(context, R.string.uri_hint_explain_edit, Toast.LENGTH_SHORT).show();
                }
                if (BuildConfig.DEBUG) {
                    Toast.makeText(context, url, Toast.LENGTH_SHORT).show();
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }
}
