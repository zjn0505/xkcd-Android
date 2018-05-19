package xyz.jienan.xkcd.home.activity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.SharedPrefManager;
import xyz.jienan.xkcd.XkcdDAO;

public class MainActivityPresenter {

    private final SharedPrefManager sharedPrefManager;

    private MainActivity view;

    private XkcdDAO xkcdDAO;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    MainActivityPresenter(MainActivity mainActivity) {
        view = mainActivity;
        xkcdDAO = new XkcdDAO();
        sharedPrefManager = new SharedPrefManager();
    }

    void loadLatestXkcd() {
        Disposable d = xkcdDAO.loadLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(xkcdPic -> {
                    long latestIndex = xkcdPic.num;
                    sharedPrefManager.setLatest(latestIndex);
                    view.latestXkcdLoaded(xkcdPic);
                }, e -> Timber.e(e, "load xkcd pic error"));
        compositeDisposable.add(d);
    }

    void comicLiked(long index) {
        if (index < 1) {
            return;
        }
        Disposable d = xkcdDAO.thumbsUp(index)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showThumbUpCount,
                        e -> Timber.e(e, "Thumbs up failed"));
        compositeDisposable.add(d);
    }

    void comicFavorited(long index, boolean isFav) {
        if (index < 1) {
            return;
        }
        Disposable d = xkcdDAO.fav(index, isFav).subscribe(xkcdPic -> {},
                e -> Timber.e(e, "error on get one pic: %d", index));
        compositeDisposable.add(d);
        view.toggleFab(isFav);
    }

    void fastLoad(int latestIndex) {
        if (latestIndex <= 0) {
            return;
        }
        Disposable d = xkcdDAO.fastLoad(latestIndex)
                .subscribe(ignore -> Timber.d("Fast load succeed"),
                        e -> Timber.e(e, "Error in fast load"));
        compositeDisposable.add(d);
    }
}
