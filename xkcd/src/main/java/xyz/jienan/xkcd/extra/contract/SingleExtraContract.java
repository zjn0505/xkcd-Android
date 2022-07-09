package xyz.jienan.xkcd.extra.contract;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.ExtraComics;

public interface SingleExtraContract {

    interface View extends BaseView<Presenter> {

        void explainLoaded(String result);

        void explainFailed();

        void renderExtraPic(ExtraComics extraComicsInDB);

        void setLoading(boolean isLoading);
    }

    interface Presenter extends BasePresenter {

        void loadExtra(int index);

        void getExplain(String url, Boolean refresh);
    }
}
