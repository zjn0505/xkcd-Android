package xyz.jienan.xkcd.extra.contract;

import java.util.List;

import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter;
import xyz.jienan.xkcd.model.ExtraComics;

public interface ExtraMainContract {

    interface View extends BaseView<Presenter> {

        void showExtras(List<ExtraComics> extraComics);
    }

    interface Presenter extends ContentMainBasePresenter {
        void observe();
        void dispose();
    }
}
