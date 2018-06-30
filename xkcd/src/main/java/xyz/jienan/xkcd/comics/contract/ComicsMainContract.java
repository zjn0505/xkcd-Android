package xyz.jienan.xkcd.comics.contract;

import java.util.List;

import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter;
import xyz.jienan.xkcd.model.XkcdPic;

public interface ComicsMainContract {

    interface View extends BaseView<Presenter> {

        void latestXkcdLoaded(XkcdPic xkcdPic);

        void showFab(XkcdPic xkcdPic);

        void toggleFab(boolean isFavorite);

        void showThumbUpCount(Long thumbCount);

        void renderXkcdSearch(List<XkcdPic> xkcdPics);
    }

    interface Presenter extends ContentMainBasePresenter {

        void fastLoad(int latestIndex);

    }
}
