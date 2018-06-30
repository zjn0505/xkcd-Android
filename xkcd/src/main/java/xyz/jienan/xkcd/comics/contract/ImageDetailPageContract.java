package xyz.jienan.xkcd.comics.contract;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;

public interface ImageDetailPageContract {

    interface View extends BaseView<Presenter> {

        void setLoading(boolean isLoading);

        void renderPic(String targetImgUrl);
    }

    interface Presenter extends BasePresenter {

        void requestImage(int index);
    }
}
