package xyz.jienan.xkcd.comics.presenter;

import android.text.TextUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;

public class ImageDetailPagePresenter implements ImageDetailPageContract.Presenter {

    private final XkcdModel xkcdModel = XkcdModel.getInstance();
    private ImageDetailPageContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ImageDetailPagePresenter(ImageDetailPageContract.View imageDetailPageActivity) {
        view = imageDetailPageActivity;
    }

    @Override
    public void requestImage(int index) {
        XkcdPic xkcdPicInDb = xkcdModel.loadXkcdFromDB(index);
        if (xkcdPicInDb == null
                || TextUtils.isEmpty(xkcdPicInDb.getTargetImg())
                || TextUtils.isEmpty(xkcdPicInDb.getTitle())) {
            Disposable d = xkcdModel.loadXkcd(index)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(ignored -> view.setLoading(true))
                    .subscribe(xkcdPic -> {
                                view.renderPic(xkcdPic.getTargetImg());
                                view.renderTitle(xkcdPic);
                            },
                            e -> Timber.e(e, "Request pic in detail page error, %d", index));
            compositeDisposable.add(d);
        } else {
            view.renderPic(xkcdPicInDb.getTargetImg());
            view.renderTitle(xkcdPicInDb);
        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
