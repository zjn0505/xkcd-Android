package xyz.jienan.xkcd.home.contract;

import android.graphics.Bitmap;

import java.util.List;

import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;

public interface SingleComicContract {

    interface View extends BaseView<Presenter> {

        void explainLoaded(String result);

        void explainFailed();

        void renderXkcdSearch(List<XkcdPic> xkcdPics);

        void renderXkcdPic(XkcdPic xkcdPicInDB);

        void setLoading(boolean isLoading);
    }

    interface Presenter extends BasePresenter {

        void loadXkcd(int index);

        void getExplain(long index);

        void updateXkcdSize(XkcdPic currentPic, Bitmap resource);

        void searchXkcd(String query);
    }
}
