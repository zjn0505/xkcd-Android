package xyz.jienan.xkcd.whatif.interfaces;

import android.webkit.JavascriptInterface;

public class ImgInterface {

    private ImgCallback imgCallback;

    public ImgInterface(ImgCallback imgCallback) {
        this.imgCallback = imgCallback;
    }

    @JavascriptInterface
    public void doLongPress(String title) {
        if (imgCallback != null) {
            imgCallback.onImgLongClick(title);
        }
    }

    public interface ImgCallback {
        void onImgLongClick(String title);
    }
}
