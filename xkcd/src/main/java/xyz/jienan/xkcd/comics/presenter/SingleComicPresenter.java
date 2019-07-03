package xyz.jienan.xkcd.comics.presenter;

import android.graphics.Bitmap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.comics.contract.SingleComicContract;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;

public class SingleComicPresenter implements SingleComicContract.Presenter {

    private final XkcdModel xkcdModel = XkcdModel.INSTANCE;
    private final SharedPrefManager sharedPrefManager = SharedPrefManager.INSTANCE;
    private SingleComicContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public SingleComicPresenter(SingleComicContract.View singleComicFragment) {
        view = singleComicFragment;
    }

    @Override
    public void getExplain(long index) {
        final long latestIndex = sharedPrefManager.getLatestXkcd();

        final Disposable d = xkcdModel.loadExplain(index, latestIndex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::explainLoaded, e -> {
                    view.explainFailed();
                    Timber.e(e, "Load explainUrl failed");
                });
        compositeDisposable.add(d);
    }

    @Override
    public void loadXkcd(int index) {
        final long latestIndex = sharedPrefManager.getLatestXkcd();
        final XkcdPic xkcdPicInDB = xkcdModel.loadXkcdFromDB(index);
        final boolean shouldQueryNetwork = latestIndex - index < 10;
        view.setLoading(true);

        if (shouldQueryNetwork || xkcdPicInDB == null) {
            final Disposable d = xkcdModel.loadXkcd(index)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(ignored -> view.setLoading(false))
                    .subscribe(xkcdPic -> {
                        if (xkcdPicInDB == null) {
                            view.renderXkcdPic(xkcdPic);
                            xkcdModel.push(xkcdPic);
                        }
                    }, e -> Timber.e(e, "load xkcd pic error"));
            compositeDisposable.add(d);
        }

        if (xkcdPicInDB != null) {
            view.renderXkcdPic(xkcdPicInDB);
            xkcdModel.push(xkcdPicInDB);
        }
    }

    @Override
    public void updateXkcdSize(XkcdPic xkcdPic, Bitmap resource) {
        if (xkcdPic != null && (xkcdPic.width == 0 || xkcdPic.height == 0) && resource != null) {
            xkcdModel.updateSize(xkcdPic.num, resource.getWidth(), resource.getHeight());
        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
