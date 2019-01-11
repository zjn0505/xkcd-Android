package xyz.jienan.xkcd.extra.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;
import xyz.jienan.xkcd.extra.contract.ExtraMainContract;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.ExtraModel;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;

public class ExtraMainPresenter implements ExtraMainContract.Presenter {

    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();

    private final ExtraModel extraModel = ExtraModel.getInstance();

    private ExtraMainContract.View view;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<ExtraComics> extraComics = new ArrayList<>();

    public ExtraMainPresenter(ExtraMainContract.View view) {
        this.view = view;
    }

    @Override
    public void loadLatest() {
        extraComics = extraModel.getAll();
        view.showExtras(extraComics);
    }

    @Override
    public void liked(long index) {

    }

    @Override
    public void favorited(long index, boolean isFav) {

    }

    @Override
    public void fastLoad(int latestIndex) {

    }

    @Override
    public void getInfoAndShowFab(int index) {

    }

    @Override
    public int getLatest() {
        return 0;
    }

    @Override
    public void setLatest(int latestIndex) {
    }

    @Override
    public void setLastViewed(int lastViewed) {
    }

    @Override
    public int getLastViewed(int latestIndex) {
        return 0;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }


    @Override
    public void searchContent(String query) {

//        if (!searchDisposable.isDisposed()) {
//            searchDisposable.dispose();
//        }
//
//        final boolean isNumQuery = isNumQuery(query);
//
//        final XkcdPic numPic = isNumQuery ? extraModel.loadXkcdFromDB(Long.parseLong(query)) : null;
//
//        searchDisposable = extraModel.search(query)
//                .startWith(numPic != null ?
//                        Observable.just(numPic).toList().toObservable() : Observable.empty())
//                .debounce(200, TimeUnit.MILLISECONDS)
//                .map(list -> {
//                    if (isNumQuery) {
//                        long num = Long.parseLong(query);
//                        XkcdPic matchedPic = null;
//                        if (numPic != null && list.contains(numPic)) {
//                            matchedPic = numPic;
//                        } else {
//                            for (XkcdPic pic : list) {
//                                if (pic.num == num) {
//                                    matchedPic = pic;
//                                    break;
//                                }
//                            }
//                        }
//                        if (matchedPic != null) {
//                            list.remove(matchedPic);
//                            list.add(0, matchedPic);
//                        }
//                    }
//                    return list;
//                })
//                .filter(xkcdPics -> xkcdPics != null && !xkcdPics.isEmpty())
//                .distinctUntilChanged()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(view::renderXkcdSearch,
//                        e -> {
//                            Timber.e(e, "search error");
//                            if (numPic != null) {
//                                view.renderXkcdSearch(Collections.singletonList(numPic));
//                            }
//                        });
//        compositeDisposable.add(searchDisposable);
    }

    @Override
    public long getRandomUntouchedIndex() {
//        final List<XkcdPic> list = extraModel.getUntouchedList();
//        if (list.isEmpty()) {
//            return 0;
//        } else {
//            return list.get(new Random().nextInt(list.size())).num;
//        }
        return 0;
    }

    private boolean isNumQuery(String query) {
        try {
            long num = Long.parseLong(query);
            return num > 0 && num <= sharedPrefManager.getLatestXkcd();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
