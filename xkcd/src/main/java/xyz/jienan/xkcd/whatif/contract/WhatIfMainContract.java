package xyz.jienan.xkcd.whatif.contract;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.WhatIfArticle;

public interface WhatIfMainContract {

    interface View extends BaseView<Presenter> {

        void latestWhatIfLoaded(WhatIfArticle whatIfArticle);

        void showFab(WhatIfArticle whatIfArticle);

        void toggleFab(boolean isFavorite);

        void showThumbUpCount(Long thumbCount);
    }

    interface Presenter extends BasePresenter {

        void whatIfFavorited(long currentIndex, boolean isFav);

        void whatIfLiked(long currentIndex);

        void setLastViewed(int lastViewed);

        void getInfoAndShowFab(int currentIndex);

        void fastLoad(int latestIndex);

        void setLatest(int latestIndex);

        int getLatest();

        int getLastViewed(int latestIndex);

        void loadLatestWhatIf();
    }
}
