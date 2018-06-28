package xyz.jienan.xkcd.list.contract;

import java.util.List;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.WhatIfArticle;

public interface WhatIfListContract {

    interface View extends BaseView<Presenter> {

        void updateData(List<WhatIfArticle> articles);

        void setLoading(boolean isLoading);

        void showScroller(int visibility);
    }

    interface Presenter extends BasePresenter {

        void loadList();

        boolean hasFav();

        void loadFavList();

        void loadPeopleChoiceList();

        boolean lastItemReached(long index);
    }
}
