package xyz.jienan.xkcd.comics.contract;

import xyz.jienan.xkcd.base.BasePresenter;
import xyz.jienan.xkcd.base.BaseView;
import xyz.jienan.xkcd.model.XkcdPic;

public interface ImageDetailPageContract {

    interface View extends BaseView<Presenter> {

        void setLoading(boolean isLoading);

        void renderPic(String targetImgUrl);

        void renderTitle(XkcdPic xkcdPic);
    }

    interface Presenter extends BasePresenter {

        void requestImage(int index);
    }
}
