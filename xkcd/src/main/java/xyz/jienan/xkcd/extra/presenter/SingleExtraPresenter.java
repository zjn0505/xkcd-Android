package xyz.jienan.xkcd.extra.presenter;

import android.text.TextUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;
import xyz.jienan.xkcd.extra.contract.SingleExtraContract;
import xyz.jienan.xkcd.model.ExtraModel;

public class SingleExtraPresenter implements SingleExtraContract.Presenter {

    private final ExtraModel extraModel = ExtraModel.getInstance();

    private SingleExtraContract.View view;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Disposable explainDisposable = Disposables.disposed();

    public SingleExtraPresenter(SingleExtraContract.View singleComicFragment) {
        view = singleComicFragment;
    }

    @Override
    public void getExplain(String url) {
        explainDisposable.dispose();
        explainDisposable = extraModel.loadExplain(url)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    explainContent -> {
                        view.explainLoaded(explainContent);
                        if (!TextUtils.isEmpty(explainContent)) {
                            extraModel.saveExtraWithExplain(url, explainContent);
                        }
                    },
                    e -> {
                        view.explainFailed();
                        Timber.e(e);
                    }
        );
        compositeDisposable.add(explainDisposable);
    }

    @Override
    public void loadExtra(int index) {
        view.renderExtraPic(extraModel.getExtra(index));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
