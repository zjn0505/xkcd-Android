package xyz.jienan.xkcd.list;

import java.util.List;

import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;

public interface XkcdListContract {

    interface View extends BaseView<Presenter> {

        void updateData(List<XkcdPic> xkcdPics);

        void setLoading(boolean isLoading);

        void showScroller(int visibility);

        void isLoadingMore(boolean isLoadingMore);
    }

    interface Presenter extends BasePresenter {

        void loadList(int startIndex);

        boolean hasFav();

        void loadFavList();

        void loadPeopleChoiceList();

        boolean lastItemReached(long index);
    }
}
