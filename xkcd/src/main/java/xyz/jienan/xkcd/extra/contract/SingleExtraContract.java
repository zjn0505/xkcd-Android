package xyz.jienan.xkcd.extra.contract;

import android.graphics.Bitmap;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.XkcdPic;

public interface SingleExtraContract {

    interface View extends BaseView<Presenter> {

        void explainLoaded(String result);

        void explainFailed();

        void renderExtraPic(ExtraComics extraComicsInDB);

        void setLoading(boolean isLoading);
    }

    interface Presenter extends BasePresenter {

        void loadXkcd(int index);

        void getExplain(long index);

        void updateXkcdSize(ExtraComics currentPic, Bitmap resource);
    }
}
