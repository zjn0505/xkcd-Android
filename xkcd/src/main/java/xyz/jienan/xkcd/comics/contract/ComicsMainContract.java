package xyz.jienan.xkcd.comics.contract;

import java.util.List;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.XkcdPic;

public interface ComicsMainContract {

    interface View extends BaseView<Presenter> {

        void latestXkcdLoaded(XkcdPic xkcdPic);

        void showFab(XkcdPic xkcdPic);

        void toggleFab(boolean isFavorite);

        void showThumbUpCount(Long thumbCount);

        void renderXkcdSearch(List<XkcdPic> xkcdPics);
    }

    interface Presenter extends BasePresenter {

        void comicFavorited(long currentIndex, boolean isFav);

        void comicLiked(long currentIndex);

        void setLastViewed(int lastViewed);

        void getInfoAndShowFab(int currentIndex);

        void fastLoad(int latestIndex);

        void setLatest(int latestIndex);

        int getLatest();

        int getLastViewed(int latestIndex);

        void loadLatestXkcd();

        void searchXkcd(String query);
    }
}
