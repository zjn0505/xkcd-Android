package xyz.jienan.xkcd.comics.contract;

import android.graphics.Bitmap;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.XkcdPic;

public interface SingleComicContract {

    interface View extends BaseView<Presenter> {

        void explainLoaded(String result);

        void explainFailed();

        void renderXkcdPic(XkcdPic xkcdPicInDB);

        void setLoading(boolean isLoading);
    }

    interface Presenter extends BasePresenter {

        void loadXkcd(int index);

        void getExplain(long index);

        void updateXkcdSize(XkcdPic currentPic, Bitmap resource);
    }
}
