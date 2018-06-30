package xyz.jienan.xkcd.whatif.interfaces;

import android.webkit.JavascriptInterface;

public class RefInterface {

    private RefCallback callback;

    public RefInterface(RefCallback callback) {
        this.callback = callback;
    }

    @JavascriptInterface
    public void refContent(String refs) {
        if (callback != null) {
            callback.onRefClick(refs);
        }
    }

    public interface RefCallback {
        void onRefClick(String content);
    }
}
