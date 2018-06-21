package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.WebView;

import xyz.jienan.xkcd.whatif.interfaces.LatexInterface;

public class WhatIfWebView extends WebView {

    private ScrollToEndCallback callback;

    private LatexInterface latexScrollInterface;

    public void setLatexScrollInterface(LatexInterface latexScrollInterface) {
        this.latexScrollInterface = latexScrollInterface;
    }

    public interface ScrollToEndCallback {
        void scrolledToTheEnd(boolean isTheEnd);
    }

    public WhatIfWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(Color.TRANSPARENT);
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
}
