package xyz.jienan.xkcd.list.presenter;

import android.view.View;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import xyz.jienan.xkcd.list.contract.XkcdListContract;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;

public class XkcdListPresenter implements XkcdListContract.Presenter {

    private final XkcdModel xkcdModel = XkcdModel.getInstance();
    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();
    private boolean inRequest = false;
    private XkcdListContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public XkcdListPresenter(XkcdListContract.View view) {
        this.view = view;
    }

    @Override
    public void loadList(final int start) {
        if (start == 1) {
            view.setLoading(true);
        }
        long latestIndex = sharedPrefManager.getLatestXkcd();
        List<XkcdPic> data = xkcdModel.loadXkcdFromDB(start, start + 399);
        int dataSize = data.size();
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", start, dataSize);
        if ((start <= latestIndex - 399 && dataSize != 400 && start != 401) ||
                (start == 401 && dataSize != 399) ||
                (start > latestIndex - 399 && start + dataSize - 1 != latestIndex)) {
            if (inRequest) {
                return;
            }
            inRequest = true;
            Disposable d = xkcdModel.loadRange(start, 400)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(list -> list.get(list.size() - 1).num)
                    .doOnDispose(() -> inRequest = false)
                    .singleOrError()
                    .subscribe(this::updateView,
                            e -> Timber.e(e, "update xkcd failed"));
            compositeDisposable.add(d);
        } else if (dataSize > 0) {
            updateView(data.get(dataSize - 1).num);
        }
    }

    @Override
    public void loadFavList() {
        view.updateData(xkcdModel.getFavXkcd());
        view.setLoading(false);
    }

    @Override
    public void loadPeopleChoiceList() {
        Disposable d = xkcdModel.getThumbUpList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(ignored -> view.setLoading(true))
                .doOnNext(ignored -> view.setLoading(false))
                .subscribe(view::updateData,
                        e -> Timber.e(e, "get top xkcd error"));
        compositeDisposable.add(d);
    }

    @Override
    public boolean hasFav() {
        List<XkcdPic> list = xkcdModel.getFavXkcd();
        if (!list.isEmpty()) {
            Disposable d = xkcdModel.validateXkcdList(list)
                    .subscribe(ignore -> {
                            },
                            e -> Timber.e(e, "error on get pic info"));
            compositeDisposable.add(d);
        }
        return !list.isEmpty();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }

    @Override
    public boolean lastItemReached(long index) {
        return index >= sharedPrefManager.getLatestXkcd();
    }

    private void updateView(long lastIndex) {
        List<XkcdPic> xkcdPics = xkcdModel.loadXkcdFromDB(1, lastIndex);
        view.showScroller(xkcdPics.isEmpty() ? View.GONE : View.VISIBLE);
        view.updateData(xkcdPics);
        view.isLoadingMore(false);
        view.setLoading(false);
    }
}
