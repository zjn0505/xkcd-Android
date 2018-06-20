package xyz.jienan.xkcd.whatif;

import android.webkit.JavascriptInterface;

import timber.log.Timber;

public class RefInterface {

    @JavascriptInterface
    public void performClick(String refs) {
        Timber.d("ref click " + refs);
    }
}
