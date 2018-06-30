package xyz.jienan.xkcd.whatif.contract;

import java.util.List;

import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.home.base.ContentMainBasePresenter;
import xyz.jienan.xkcd.model.WhatIfArticle;

public interface WhatIfMainContract {

    interface View extends BaseView<Presenter> {

        void latestWhatIfLoaded(WhatIfArticle whatIfArticle);

        void showFab(WhatIfArticle whatIfArticle);

        void toggleFab(boolean isFavorite);

        void showThumbUpCount(Long thumbCount);

        void renderWhatIfSearch(List<WhatIfArticle> articles);
    }

    interface Presenter extends ContentMainBasePresenter {
    }
}
