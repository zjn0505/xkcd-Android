package xyz.jienan.xkcd.comics.presenter;

import android.graphics.Bitmap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.comics.contract.SingleComicContract;

public class SingleComicPresenter implements SingleComicContract.Presenter {

    private SingleComicContract.View view;

    private final XkcdModel xkcdModel = XkcdModel.getInstance();

    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();

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
                    Timber.e(e, "Load explain failed");
                });
        compositeDisposable.add(d);
    }

    @Override
    public void searchXkcd(String query) {
        final Disposable d = xkcdModel.search(query)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(xkcdPics -> xkcdPics != null && !xkcdPics.isEmpty())
                .subscribe(view::renderXkcdSearch,
                        e -> Timber.e(e, "search error"));
        compositeDisposable.add(d);
    }

    @Override
    public void loadXkcd(int index) {
        final long latestIndex = sharedPrefManager.getLatestXkcd();
        XkcdPic xkcdPicInDB = xkcdModel.loadXkcdFromDB(index);

        boolean shouldQueryNetwork = false;

        if (xkcdPicInDB != null) {
            view.renderXkcdPic(xkcdPicInDB);
            xkcdModel.push(xkcdPicInDB);
        } else {
            shouldQueryNetwork = true;
        }

        if (latestIndex - index < 10) {
            shouldQueryNetwork = true;
        }

        if (shouldQueryNetwork) {
            final Disposable d = xkcdModel.loadXkcd(index)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(ignored -> view.setLoading(true))
                    .subscribe(xkcdPic -> {
                        view.renderXkcdPic(xkcdPic);
                        xkcdModel.push(xkcdPic);
                    }, e -> {
                        Timber.e(e, "load xkcd pic error");
                        view.setLoading(false);
                    });
            compositeDisposable.add(d);
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
