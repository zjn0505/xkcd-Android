package xyz.jienan.xkcd.list;

import android.view.View;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.XkcdDAO;
import xyz.jienan.xkcd.XkcdPic;

public class XkcdListActivityPresenter {

    private boolean inRequest = false;

    private int latestIndex;

    private XkcdListActivity view;

    private XkcdDAO xkcdDAO;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    XkcdListActivityPresenter(XkcdListActivity view) {
        xkcdDAO = new XkcdDAO();
        this.view = view;
    }

    public void updateLatestIndex(int latestIndex) {
        this.latestIndex = latestIndex;
    }

    public void loadList(final int start) {
        List<XkcdPic> data = xkcdDAO.loadXkcdFromDB(start, start + 399);
        int dataSize = data.size();
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", start, dataSize);
        if ((start <= latestIndex - 399 && dataSize != 400 && start != 401) ||
                (start == 401 && dataSize != 399) ||
                (start > latestIndex - 399 && start + dataSize - 1 != latestIndex)) {
            if (inRequest) {
                return;
            }
            inRequest = true;
            Disposable d = xkcdDAO.loadRange(start, 400)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(list -> list.get(list.size() - 1).num)
                    .doOnNext(ignore -> inRequest = false)
                    .subscribe(this::updateView, e -> {
                        Timber.e(e, "update xkcd failed");
                        inRequest = false;
                    }, () -> inRequest = false);
            compositeDisposable.add(d);
        } else {
            updateView(data.get(dataSize -1).num);
        }
    }

    public void loadFavList() {
        view.updateData(xkcdDAO.getFavXkcd());
    }

    public void loadPeopleChoiceList(){
        view.setLoading(true);
        Disposable d = xkcdDAO.getThumbUpList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(ignored -> view.setLoading(false))
                .subscribe(view::updateData,
                        e -> Timber.e(e, "get top xkcd error"));
        compositeDisposable.add(d);
    }

    public boolean hasFav() {
        List<XkcdPic> list = xkcdDAO.getFavXkcd();
        if (!list.isEmpty()) {
            Disposable d = xkcdDAO.validateXkcdList(list)
                    .subscribe(ignore -> {},
                            e -> Timber.e(e, "error on get pic info"));
            compositeDisposable.add(d);
        }
        return !list.isEmpty();
    }

    private void updateView(long lastIndex) {
        List<XkcdPic> xkcdPics = xkcdDAO.loadXkcdFromDB(1, lastIndex);
        view.showScroller(xkcdPics.isEmpty() ? View.GONE : View.VISIBLE);
        view.updateData(xkcdPics);
        view.isLoadingMore(false);
    }
}
