package xyz.jienan.xkcd.whatif.presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;

public class WhatIfMainPresenter implements WhatIfMainContract.Presenter {

    private WhatIfMainContract.View view;

    private WhatIfModel whatIfModel = WhatIfModel.getInstance();

    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private WhatIfMainPresenter() {
        // no default public constructor
    }

    public WhatIfMainPresenter(WhatIfMainContract.View view) {
        this.view = view;
    }

    @Override
    public void whatIfFavorited(long currentIndex, boolean isFav) {

    }

    @Override
    public void whatIfLiked(long currentIndex) {

    }

    @Override
    public void setLastViewed(int lastViewed) {

    }

    @Override
    public void getInfoAndShowFab(int currentIndex) {

    }

    @Override
    public void fastLoad(int latestIndex) {

    }

    @Override
    public void setLatest(int latestIndex) {

    }

    @Override
    public int getLatest() {
        return 0;
    }

    @Override
    public int getLastViewed(int latestIndex) {
        return 0;
    }

    @Override
    public void loadLatestWhatIf() {
        Disposable d = whatIfModel.loadLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(whatIfArticle -> {
                    long latestIndex = whatIfArticle.num;
                    sharedPrefManager.setLatestXkcd(latestIndex);
                    view.latestWhatIfLoaded(whatIfArticle);
                }, e -> Timber.e(e, "load what if article error"));
        compositeDisposable.add(d);
    }

    @Override
    public void onDestroy() {

    }
}
