package xyz.jienan.xkcd.whatif;

import android.webkit.JavascriptInterface;

import timber.log.Timber;

public class LatexInterface {

    private boolean canParentScroll = true;

    @JavascriptInterface
    public void onTouch(int i) {

        Timber.d("ssssssssssssssssssssss " + i);
        canParentScroll = i == 3;
    }

    public boolean canScrollHor() {
        Timber.d("get scrol sssssssssssssss " + canParentScroll);
        return canParentScroll;
    }
}
