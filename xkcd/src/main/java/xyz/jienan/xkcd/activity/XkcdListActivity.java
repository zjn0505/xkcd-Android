package xyz.jienan.xkcd.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.XkcdPic_;
import xyz.jienan.xkcd.fragment.ListFilterDialogFragment;
import xyz.jienan.xkcd.network.NetworkService;
import xyz.jienan.xkcd.ui.RecyclerViewFastScroller;
import xyz.jienan.xkcd.ui.XkcdListGridAdapter;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;
import static xyz.jienan.xkcd.activity.XkcdListActivity.Selection.ALL_COMICS;
import static xyz.jienan.xkcd.network.NetworkService.XKCD_BROWSE_LIST;

/**
 * Created by jienanzhang on 22/03/2018.
 */

public class XkcdListActivity extends BaseActivity {

    private static final int INVALID_ID = 0;
    private final static int COUNT_IN_ADV = 10;
    private XkcdListGridAdapter mAdapter;
    private Box<XkcdPic> box;
    private RecyclerView rvList;
    private StaggeredGridLayoutManager sglm;
    private int spanCount = 2;
    private boolean loadingMore = false;
    private boolean inRequest = false;
    private int latestIndex;

    private SharedPreferences sharedPreferences;
    private Selection currentSelection = ALL_COMICS;

    private List<XkcdPic> pics = new ArrayList<>();
    private RecyclerViewFastScroller scroller;
    private RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {

        private static final int FLING_JUMP_LOW_THRESHOLD = 80;
        private static final int FLING_JUMP_HIGH_THRESHOLD = 120;

        private boolean dragging = false;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            dragging = newState == SCROLL_STATE_DRAGGING;
            if (mAdapter.getGlide().isPaused()) {
                if (newState == SCROLL_STATE_DRAGGING || newState == SCROLL_STATE_IDLE) {
                    // user is touchy or the scroll finished, show images
                    mAdapter.getGlide().resumeRequests();
                } // settling means the user let the screen go, but it can still be flinging
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!dragging) {
                int currentSpeed = Math.abs(dy);
                boolean paused = mAdapter.getGlide().isPaused();
                if (paused && currentSpeed < FLING_JUMP_LOW_THRESHOLD) {
                    mAdapter.getGlide().resumeRequests();
                } else if (!paused && FLING_JUMP_HIGH_THRESHOLD < currentSpeed) {
                    mAdapter.getGlide().pauseRequests();
                }
            }
            if (currentSelection != ALL_COMICS) {
                return;
            }
            int visibleItemCount = sglm.getChildCount();
            int[] firstVisibileItemPositions = new int[spanCount];
            firstVisibileItemPositions = sglm.findFirstVisibleItemPositions(firstVisibileItemPositions);
            if (firstVisibileItemPositions[1] + visibleItemCount >= mAdapter.getItemCount() - COUNT_IN_ADV
                    && !loadingMore
                    && !lastItemReached()) {
                loadingMore = true;
                loadList((int) (pics.get(pics.size() - 1).num + 1));
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentSelection != ALL_COMICS) {
            outState.putInt("Selection", currentSelection.id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Query<XkcdPic> query = box.query().equal(XkcdPic_.isFavorite, true).build();
        List<XkcdPic> list = query.find();
        if (list.size() > 0) {
            getMenuInflater().inflate(R.menu.menu_list, menu);
            Observable.fromIterable(list).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                    .filter(new Predicate<XkcdPic>() {
                @Override
                public boolean test(XkcdPic xkcdPic) throws Exception {
                    return xkcdPic == null || xkcdPic.width == 0 || xkcdPic.height == 0;
                }
            }).toSortedList().observeOn(Schedulers.io()).flatMap(new Function<List<XkcdPic>, SingleSource<List<XkcdPic>>>() {
                @Override
                public SingleSource<List<XkcdPic>> apply(List<XkcdPic> xkcdPics) throws Exception {
                    return NetworkService.getXkcdAPI()
                            .getXkcdList(XKCD_BROWSE_LIST, (int) xkcdPics.get(0).num, 0, (int) xkcdPics.get(xkcdPics.size() - 1).num)
                            .singleOrError();
                }
            }).subscribe(new SingleObserver<List<XkcdPic>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(List<XkcdPic> xkcdPics) {
                    for (XkcdPic pic : xkcdPics) {
                        XkcdPic xkcdPic = box.get(pic.num);
                        if (xkcdPic != null) {
                            pic.isFavorite = xkcdPic.isFavorite;
                            pic.hasThumbed = xkcdPic.hasThumbed;
                        }
                    }
                    box.put(xkcdPics);
                }

                @Override
                public void onError(Throwable e) {

                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_filter:
                ListFilterDialogFragment filterDialog = new ListFilterDialogFragment();
                filterDialog.show(getSupportFragmentManager(), "filter");
                getSupportFragmentManager().executePendingTransactions();
                filterDialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        int selection = sharedPreferences.getInt("FILTER_SELECTION", 0);
                        if (currentSelection.ordinal() != selection) {
                            currentSelection = Selection.fromValue(selection);
                            reloadList(currentSelection);
                        }
                    }
                });
                break;
        }
        return true;
    }

    private void reloadList(Selection currentSelection) {
        switch (currentSelection) {
            case ALL_COMICS:
                loadList(1);
                break;
            case MY_FAVORITE:
                Query<XkcdPic> query = box.query().equal(XkcdPic_.isFavorite, true).build();
                List<XkcdPic> list = query.find();
                mAdapter.updateData(list);
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_list);
        rvList = findViewById(R.id.rv_list);
        scroller = findViewById(R.id.rv_scroller);
        scroller.setRecyclerView(rvList);
        scroller.setViewsToUse(R.layout.rv_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        mAdapter = new XkcdListGridAdapter(this);
        rvList.setAdapter(mAdapter);
        rvList.setHasFixedSize(true);
        sglm = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        rvList.setLayoutManager(sglm);
        rvList.addOnScrollListener(rvScrollListener);
        latestIndex = PreferenceManager.getDefaultSharedPreferences(this).getInt(XKCD_LATEST_INDEX, INVALID_ID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            int selection = savedInstanceState.getInt("Selection", ALL_COMICS.id);
            currentSelection = Selection.fromValue(selection);
        } else {
            sharedPreferences.edit().putInt("FILTER_SELECTION", ALL_COMICS.id).apply();
        }
        reloadList(currentSelection);
    }

    @Override
    protected void onDestroy() {
        rvList.removeOnScrollListener(rvScrollListener);
        super.onDestroy();
    }

    private boolean lastItemReached() {
        if (mAdapter.getPics() != null) {
            List<XkcdPic> pics = mAdapter.getPics();
            XkcdPic lastPic = pics.get(pics.size() - 1);
            return lastPic.num >= latestIndex;
        }
        return false;
    }

    private void loadList(final int start) {
        Query<XkcdPic> query = box.query().between(XkcdPic_.num, start, start + 399).build();
        List<XkcdPic> data = query.find();
        int dataSize = data.size();
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", start, dataSize);
        if ((start <= latestIndex - 399 && dataSize != 400 && start != 401) ||
                (start == 401 && dataSize != 399) ||
                (start > latestIndex - 399 && start + dataSize - 1 != latestIndex)) {
            if (inRequest) {
                return;
            }
            NetworkService.getXkcdAPI().getXkcdList(XKCD_BROWSE_LIST, start, 0, 400)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<XkcdPic>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            inRequest = true;
                        }

                        @Override
                        public void onNext(List<XkcdPic> xkcdPics) {
                            appendList(xkcdPics, true);
                            loadingMore = false;
                            inRequest = false;
                        }

                        @Override
                        public void onError(Throwable e) {
                            inRequest = false;
                        }

                        @Override
                        public void onComplete() {
                            inRequest = false;
                        }
                    });
        } else {
            appendList(data, false);
            loadingMore = false;
        }
    }

    @SuppressLint("CheckResult")
    public void appendList(final List<XkcdPic> xkcdPics, boolean checkDB) {
        if (checkDB) {
            final Query<XkcdPic> query = box.query().between(XkcdPic_.num, xkcdPics.get(0).num, xkcdPics.get(xkcdPics.size() - 1).num).build();
            final List<XkcdPic> list = query.find();
            final HashMap<Long, XkcdPic> map = new HashMap<Long, XkcdPic>();
            for (XkcdPic xkcdPic : list) {
                map.put(xkcdPic.num, xkcdPic);
            }
            Observable.fromArray(xkcdPics.toArray(new XkcdPic[xkcdPics.size()]))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(new Function<XkcdPic, XkcdPic>() {
                        @Override
                        public XkcdPic apply(XkcdPic xkcdPic) throws Exception {
                            XkcdPic pic = map.get(xkcdPic.num);
                            if (pic != null) {
                                xkcdPic.isFavorite = pic.isFavorite;
                                xkcdPic.hasThumbed = pic.hasThumbed;
                            }
                            if (!pics.contains(xkcdPic)) {
                                pics.add(xkcdPic);
                            }
                            return xkcdPic;
                        }
                    }).toList().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<XkcdPic>>() {
                        @Override
                        public void accept(List<XkcdPic> xkcdPics) throws Exception {
                            box.put(xkcdPics);
                            scroller.setVisibility(pics.isEmpty() ? View.GONE : View.VISIBLE);
                            mAdapter.updateData(pics);
                        }
                    });
        } else {
            for (XkcdPic pic : xkcdPics) {
                if (!pics.contains(pic)) {
                    pics.add(pic);
                }
            }
            scroller.setVisibility(pics.isEmpty() ? View.GONE : View.VISIBLE);
            mAdapter.updateData(pics);
        }
    }

    public enum Selection {
        ALL_COMICS(0),
        MY_FAVORITE(1),
        PEOPLES_CHOICE(2);

        public int id;
        Selection(int id) {
            this.id = id;
        }

        public static Selection fromValue(int value) {
            for (Selection selection : values()) {
                if (selection.id == value) {
                    return selection;
                }
            }
            return null;
        }
    }
}
