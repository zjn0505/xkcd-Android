package xyz.jienan.xkcd.list;

import android.view.View;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.BoxManager;
import xyz.jienan.xkcd.XkcdDAO;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.base.network.NetworkService;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_BROWSE_LIST;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP_SORT_BY_THUMB_UP;

public class XkcdListActivityPresenter {

    private final BoxManager boxManager;

    private boolean inRequest = false;

    private int latestIndex;

    private XkcdListActivity view;

    private XkcdDAO xkcdDAO;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    XkcdListActivityPresenter(XkcdListActivity view) {
        boxManager = new BoxManager();
        xkcdDAO = new XkcdDAO(boxManager);
        this.view = view;
    }

    public void updateLatestIndex(int latestIndex) {
        this.latestIndex = latestIndex;
    }

    public void loadList(final int start) {
        List<XkcdPic> data = boxManager.getXkcdInRange(start, start + 399);
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
        List<XkcdPic> listFav = boxManager.getFavXkcd();
        view.updateData(listFav);
    }

    public void loadPeopleChoiceList(){
        Disposable d = NetworkService.getXkcdAPI()
                .getTopXkcds(XKCD_TOP, XKCD_TOP_SORT_BY_THUMB_UP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(xkcdPics -> {
                    boxManager.updateAndSave(xkcdPics);
                    view.updateData(xkcdPics);
                }, e -> Timber.e(e, "get top xkcd error"));
        compositeDisposable.add(d);
    }

    public boolean hasFav() {
        List<XkcdPic> list = boxManager.getFavXkcd();
        Disposable d = Observable.fromIterable(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter(xkcdPic -> xkcdPic == null || xkcdPic.width == 0 || xkcdPic.height == 0)
                .toSortedList()
                .observeOn(Schedulers.io())
                .flatMap((Function<List<XkcdPic>, SingleSource<List<XkcdPic>>>) xkcdPics -> NetworkService.getXkcdAPI()
                        .getXkcdList(XKCD_BROWSE_LIST, (int) xkcdPics.get(0).num, 0, (int) xkcdPics.get(xkcdPics.size() - 1).num)
                        .singleOrError())
                .subscribe(boxManager::updateAndSave,
                        e -> Timber.e(e, "error on get pic info"));
        compositeDisposable.add(d);
        return !list.isEmpty();
    }

    private void updateView(long lastIndex) {
        List<XkcdPic> xkcdPics = boxManager.getXkcdInRange(1, lastIndex);
        view.showScroller(xkcdPics.isEmpty() ? View.GONE : View.VISIBLE);
        view.updateData(xkcdPics);
        view.isLoadingMore(false);
    }
}
