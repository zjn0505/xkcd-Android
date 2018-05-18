package xyz.jienan.xkcd.home.activity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.BoxManager;
import xyz.jienan.xkcd.SharedPrefManager;
import xyz.jienan.xkcd.XkcdDAO;
import xyz.jienan.xkcd.XkcdPic;

public class MainActivityPresenter {

    private final BoxManager boxManager;

    private final SharedPrefManager sharedPrefManager;

    private MainActivity view;

    private XkcdDAO xkcdDAO;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    MainActivityPresenter(MainActivity mainActivity) {
        view = mainActivity;
        boxManager = new BoxManager();
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
        boxManager.like(index);
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
        XkcdPic xkcdPicInBox = boxManager.fav(index, isFav);
        view.toggleFab(isFav);
        if (xkcdPicInBox.width == 0 || xkcdPicInBox.height == 0) {
            Disposable d = xkcdDAO.loadXkcd(index)
                    .subscribe(xkcdPic -> {},
                            e -> Timber.e(e, "error on get one pic: %d", xkcdPicInBox.num));
            compositeDisposable.add(d);
        }
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
