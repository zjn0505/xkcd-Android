package xyz.jienan.xkcd.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.model.util.XkcdExplainUtil;
import xyz.jienan.xkcd.whatif.interfaces.LatexInterface;

public class WhatIfWebView extends WebView {

    private ScrollToEndCallback callback;

    private LatexInterface latexScrollInterface;

    private RequestManager glide;

    private ArrayList<FutureTarget<File>> imageTasks = new ArrayList<>();

    public WhatIfWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(Color.TRANSPARENT);
        glide = Glide.with(context);
        setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                        && url.contains("what-if.xkcd.com/imgs/a/")
                        || url.contains("imgs.xkcd.com/comics")) {
                    url = url.replace("http://", "https://");
                    FutureTarget<File> t = glide.load(url).downloadOnly(10, 10);
                    imageTasks.add(t);
                    try {
                        return new WebResourceResponse("image/png", "deflate", new FileInputStream((t.get())));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    return null;
                } else {
                    return super.shouldInterceptRequest(view, url);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (XkcdExplainUtil.isXkcdImageLink(url)) {
                    final long id = XkcdExplainUtil.getXkcdIdFromXkcdImageLink(url);
                    if (id != -1) {
                        ImageDetailPageActivity.startActivityFromId(getContext(), id);
                    } else {
                        ImageDetailPageActivity.startActivity(getContext(), url, 1L, false);
                    }
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }

                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (browserIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                        view.getContext().startActivity(browserIntent);
                    }
                }
                return true;
            }
        });
    }

    public void setLatexScrollInterface(LatexInterface latexScrollInterface) {
        this.latexScrollInterface = latexScrollInterface;
    }

    public void setCallback(ScrollToEndCallback callback) {
        this.callback = callback;
    }

    public int distanceToEnd() {
        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        int webViewHeight = this.getMeasuredHeight();
        return height - getScrollY() - webViewHeight;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (callback != null) {
            if (distanceToEnd() < 200) {
                callback.scrolledToTheEnd(true);
            } else if (distanceToEnd() > 250) {
                callback.scrolledToTheEnd(false);
            }
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void destroy() {
        callback = null;
        super.destroy();
    }

    public boolean canScrollHor(int direction) {
        if (zoomScroll(direction)) {
            return true;
        }
        if (latexScrollInterface != null) {
            return !latexScrollInterface.canScrollHor();
        }
        return zoomScroll(direction);
    }

    private boolean zoomScroll(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    public void clearTasks() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            for (FutureTarget t : imageTasks) {
                t.cancel(true);
                t.clear();
                Timber.e("OkHttp: cleared tasks %d", imageTasks.size());
            }
        }
    }

    public interface ScrollToEndCallback {
        void scrolledToTheEnd(boolean isTheEnd);
    }
}
