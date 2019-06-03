package xyz.jienan.xkcd.list.presenter;

import android.view.View;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.list.contract.WhatIfListContract;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;

public class WhatIfListPresenter implements WhatIfListContract.Presenter {

    private final WhatIfModel whatIfModel = WhatIfModel.INSTANCE;
    private final SharedPrefManager sharedPrefManager = SharedPrefManager.INSTANCE;
    private WhatIfListContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public WhatIfListPresenter(WhatIfListContract.View view) {
        this.view = view;
    }

    @Override
    public void loadList() {
        view.setLoading(true);
        List<WhatIfArticle> data = whatIfModel.loadArticlesFromDB();
        int dataSize = data.size();
        if (dataSize == 0) {
            Disposable d = whatIfModel.loadAllWhatIf()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::updateView,
                            e -> Timber.e(e, "update what if failed"));
            compositeDisposable.add(d);
        } else {
            updateView(data);
        }
    }

    @Override
    public void loadFavList() {
        view.updateData(whatIfModel.getFavWhatIf());
        view.setLoading(false);
    }

    @Override
    public void loadPeopleChoiceList() {
        Disposable d = whatIfModel.getThumbUpList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(ignored -> view.setLoading(true))
                .flatMapSingle(whatIfArticles -> Observable.fromIterable(whatIfArticles)
                        .map(article -> article.num)
                        .filter(num -> num <= sharedPrefManager.getLatestWhatIf())
                        .map(whatIfModel::loadArticleFromDB)
                        .toList())
                .doOnNext(ignored -> view.setLoading(false))
                .subscribe(view::updateData,
                        e -> Timber.e(e, "get top what if error"));
        compositeDisposable.add(d);
    }

    @Override
    public boolean hasFav() {
        return !whatIfModel.getFavWhatIf().isEmpty();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }

    @Override
    public boolean lastItemReached(long index) {
        return index >= sharedPrefManager.getLatestWhatIf();
    }

    private void updateView(final List<WhatIfArticle> articles) {
        view.showScroller(articles.isEmpty() ? View.GONE : View.VISIBLE);
        view.updateData(articles);
        view.setLoading(false);
    }
}
