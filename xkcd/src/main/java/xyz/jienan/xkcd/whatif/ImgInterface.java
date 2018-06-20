package xyz.jienan.xkcd.whatif;

import android.webkit.JavascriptInterface;

import timber.log.Timber;

public class ImgInterface {

    private ImgCallback imgCallback;

    public ImgInterface(ImgCallback imgCallback) {
        this.imgCallback = imgCallback;
    }

    @JavascriptInterface
    public void doLongPress(String title) {
        imgCallback.onImgLongClick(title);
        Timber.d("do long press " + title);
    }

    public interface ImgCallback {
        void onImgLongClick(String title);
    }
}
