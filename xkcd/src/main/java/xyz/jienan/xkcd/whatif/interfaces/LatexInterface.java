package xyz.jienan.xkcd.whatif.interfaces;

import android.webkit.JavascriptInterface;

public class LatexInterface {

    private boolean canParentScroll = true;

    @JavascriptInterface
    public void onTouch(int i) {
        canParentScroll = i == 3;
    }

    public boolean canScrollHor() {
        return canParentScroll;
    }
}
