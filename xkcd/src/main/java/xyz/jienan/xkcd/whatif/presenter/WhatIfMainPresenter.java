package xyz.jienan.xkcd.whatif.presenter;

import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;

public class WhatIfMainPresenter implements WhatIfMainContract.Presenter {

    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();

    private WhatIfMainContract.View view;

    private WhatIfModel whatIfModel = WhatIfModel.getInstance();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Disposable fabShowDisposable = Disposables.empty();

    private Disposable searchDisposable = Disposables.empty();

    private WhatIfMainPresenter() {
        // no default public constructor
    }

    public WhatIfMainPresenter(WhatIfMainContract.View view) {
        this.view = view;
    }

    @Override
    public void favorited(long index, boolean isFav) {
        if (index < 1) {
            return;
        }
        Disposable d = whatIfModel.fav(index, isFav).subscribe(xkcdPic -> {
                },
                e -> Timber.e(e, "error on get one pic: %d", index));
        compositeDisposable.add(d);
        view.toggleFab(isFav);
    }

    @Override
    public void liked(long currentIndex) {
        if (currentIndex < 1) {
            return;
        }
        compositeDisposable.add(
                whatIfModel
                        .thumbsUp(currentIndex)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::showThumbUpCount,
                                e -> Timber.e(e, "Thumbs up failed")));
    }

    @Override
    public void getInfoAndShowFab(int index) {
        if (fabShowDisposable != null && !fabShowDisposable.isDisposed()) {
            fabShowDisposable.dispose();
        }
        WhatIfArticle article = whatIfModel.loadArticleFromDB(index);
        if (article == null) {
            fabShowDisposable = whatIfModel.observe()
                    .filter(article1 -> article1.num == index)
                    .subscribe(view::showFab,
                            e -> Timber.e(e, "what if pipeline observing error"));
            compositeDisposable.add(fabShowDisposable);
        } else {
            view.showFab(article);
        }
    }

    @Override
    public void setLastViewed(int lastViewed) {
        sharedPrefManager.setLastViewedWhatIf(lastViewed);
    }

    @Override
    public int getLatest() {
        return (int) sharedPrefManager.getLatestWhatIf();
    }

    @Override
    public void setLatest(int latestIndex) {
        sharedPrefManager.setLastViewedWhatIf(latestIndex);
    }

    @Override
    public int getLastViewed(int latestIndex) {
        return (int) sharedPrefManager.getLastViewedWhatIf(latestIndex);
    }

    @Override
    public void loadLatest() {
        Disposable d = whatIfModel.loadLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(whatIfArticle -> {
                    long latestIndex = whatIfArticle.num;
                    sharedPrefManager.setLatestWhatIf(latestIndex);
                    view.latestWhatIfLoaded(whatIfArticle);
                }, e -> Timber.e(e, "load what if article error"));
        compositeDisposable.add(d);
    }

    @Override
    public void searchContent(String query) {

        if (!searchDisposable.isDisposed()) {
            searchDisposable.dispose();
        }

        searchDisposable = whatIfModel.searchWhatIf(query, sharedPrefManager.getWhatIfSearchPref())
                .map(list -> {
                    if (isNumQuery(query)) {
                        long num = Long.parseLong(query);
                        WhatIfArticle matchNumArticle = null;
                        for (WhatIfArticle article : list) {
                            if (article.num == num) {
                                matchNumArticle = article;
                                break;
                            }
                        }
                        if (matchNumArticle != null) {
                            list.remove(matchNumArticle);
                            list.add(0, matchNumArticle);
                        }
                    }
                    return list;
                })
                .subscribe(view::renderWhatIfSearch,
                        e -> {
                    Timber.e(e, "search what if error");
                    if (isNumQuery(query)) {
                        long num = Long.parseLong(query);
                        WhatIfArticle article = whatIfModel.loadArticleFromDB(num);
                        if (article != null) {
                            view.renderWhatIfSearch(Collections.singletonList(article));
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        fabShowDisposable.dispose();
        searchDisposable.dispose();
    }

    private boolean isNumQuery(String query) {
        try {
            long num = Long.parseLong(query);
            return num > 0 && num <= sharedPrefManager.getLatestWhatIf();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
