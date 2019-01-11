package xyz.jienan.xkcd.extra.presenter;

import android.graphics.Bitmap;

import io.reactivex.disposables.CompositeDisposable;
import xyz.jienan.xkcd.extra.contract.SingleExtraContract;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.ExtraModel;

public class SingleExtraPresenter implements SingleExtraContract.Presenter {

    private final ExtraModel extraModel = ExtraModel.getInstance();

    private SingleExtraContract.View view;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public SingleExtraPresenter(SingleExtraContract.View singleComicFragment) {
        view = singleComicFragment;
    }

    @Override
    public void getExplain(long index) {
        view.explainLoaded(extraModel.getExtra((int) index).explain);
    }

    @Override
    public void updateXkcdSize(ExtraComics currentPic, Bitmap resource) {

    }

    @Override
    public void loadXkcd(int index) {
        view.renderExtraPic(extraModel.getExtra(index));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
