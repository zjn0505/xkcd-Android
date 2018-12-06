package xyz.jienan.xkcd.home.base;

import xyz.jienan.xkcd.base.BasePresenter;

public interface ContentMainBasePresenter extends BasePresenter {

    void favorited(long currentIndex, boolean isFav);

    void liked(long currentIndex);

    void setLastViewed(int lastViewed);

    void getInfoAndShowFab(int currentIndex);

    int getLatest();

    void setLatest(int latestIndex);

    int getLastViewed(int latestIndex);

    void loadLatest();

    void searchContent(String query);

    long getRandomUntouchedIndex();
}
