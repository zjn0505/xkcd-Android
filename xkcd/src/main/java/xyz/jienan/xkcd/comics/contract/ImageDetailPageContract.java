package xyz.jienan.xkcd.comics.contract;

import android.graphics.Bitmap;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.XkcdPic;

public interface ImageDetailPageContract {

    interface View extends BaseView<Presenter> {

        void setLoading(boolean isLoading);

        void renderPic(String targetImgUrl);

        void renderTitle(XkcdPic xkcdPic);

        void renderSeekBar(int d);

        void renderFrame(Bitmap bitmap);

        void changeGifSeekBarProgress(int progress);

        void showGifPlaySpeed(int speed);
    }

    interface Presenter extends BasePresenter {

        void requestImage(int index);

        void parseGifData(byte[] data);

        void parseFrame(int progress);

        void adjustGifSpeed(int increaseByOne);

        void adjustGifFrame(boolean isForward);

        void setEcoMode(boolean isEcoMode);
    }
}
