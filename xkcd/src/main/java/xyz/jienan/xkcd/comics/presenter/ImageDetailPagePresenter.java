package xyz.jienan.xkcd.comics.presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract;

public class ImageDetailPagePresenter implements ImageDetailPageContract.Presenter {

    private ImageDetailPageContract.View view;

    private final XkcdModel xkcdModel = XkcdModel.getInstance();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ImageDetailPagePresenter(ImageDetailPageContract.View imageDetailPageActivity) {
        view = imageDetailPageActivity;
    }

    public void requestImage(int index) {
        Disposable d = xkcdModel.loadXkcd(index)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(ignored -> view.setLoading(true))
                .subscribe(xkcdPic -> view.renderPic(xkcdPic.getTargetImg()),
                        e -> Timber.e(e, "Request pic in detail page error, %d", index));
        compositeDisposable.add(d);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
