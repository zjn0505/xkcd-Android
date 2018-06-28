package xyz.jienan.xkcd.list.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseActivity;
import xyz.jienan.xkcd.list.ListFilterDialogFragment;
import xyz.jienan.xkcd.list.RecyclerViewFastScroller;
import xyz.jienan.xkcd.list.XkcdListGridAdapter;
import xyz.jienan.xkcd.list.contract.XkcdListContract;
import xyz.jienan.xkcd.list.presenter.XkcdListPresenter;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.ui.RecyclerItemClickListener;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_ALL;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_FAV;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_THUMB;
import static xyz.jienan.xkcd.Const.FIRE_LIST_FILTER_BAR;
import static xyz.jienan.xkcd.Const.FIRE_SCROLL_TO_END;
import static xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID;
import static xyz.jienan.xkcd.list.activity.XkcdListActivity.Selection.ALL_COMICS;

/**
 * Created by jienanzhang on 22/03/2018.
 */

public class XkcdListActivity extends BaseActivity implements XkcdListContract.View, ListFilterDialogFragment.OnItemSelectListener {

    private final static int COUNT_IN_ADV = 10;

    @BindView(R.id.rv_list)
    RecyclerView rvList;

    @BindView(R.id.rv_scroller)
    RecyclerViewFastScroller scroller;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private XkcdListGridAdapter mAdapter;

    private StaggeredGridLayoutManager sglm;

    private int spanCount = 2;

    private boolean loadingMore = false;

    private SharedPreferences sharedPreferences;

    private Selection currentSelection = ALL_COMICS;

    private XkcdListContract.Presenter xkcdListPresenter;

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

            if (!rvList.canScrollVertically(1) && lastItemReached() && newState == SCROLL_STATE_IDLE) {
                logUXEvent(FIRE_SCROLL_TO_END);
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
                xkcdListPresenter.loadList((int) (mAdapter.getPics().get(mAdapter.getItemCount() - 1).num + 1));
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
        if (xkcdListPresenter.hasFav()) {
            getMenuInflater().inflate(R.menu.menu_list, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_filter:
                final FragmentManager fragmentManager = getSupportFragmentManager();
                ListFilterDialogFragment filterDialog = (ListFilterDialogFragment) fragmentManager.findFragmentByTag("filter");
                if (filterDialog == null) {
                    filterDialog = new ListFilterDialogFragment();
                }
                if (!filterDialog.isAdded()) {
                    filterDialog.show(getSupportFragmentManager(), "filter");
                    filterDialog.setItemSelectListener(this);
                    filterDialog.setSelection(currentSelection.ordinal());
                    logUXEvent(FIRE_LIST_FILTER_BAR);
                }
                break;
        }
        return true;
    }

    private void reloadList(@NonNull Selection currentSelection) {
        switch (currentSelection) {
            case ALL_COMICS:
                xkcdListPresenter.loadList(1);
                break;
            case MY_FAVORITE:
                xkcdListPresenter.loadFavList();
                break;
            case PEOPLES_CHOICE:
                xkcdListPresenter.loadPeopleChoiceList();
                break;
        }
        rvList.scrollToPosition(0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xkcdListPresenter = new XkcdListPresenter(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        scroller.setRecyclerView(rvList);
        scroller.setViewsToUse(R.layout.rv_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        mAdapter = new XkcdListGridAdapter(this);
        rvList.setAdapter(mAdapter);
        rvList.setHasFixedSize(true);
        rvList.addOnItemTouchListener(new RecyclerItemClickListener(this, rvList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= 0) {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_TARGET_XKCD_ID, (int) mAdapter.getPic(position).num);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // no-ops
            }
        }));
        sglm = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        rvList.setLayoutManager(sglm);
        rvList.addOnScrollListener(rvScrollListener);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            int selection = savedInstanceState.getInt("Selection", ALL_COMICS.id);
            currentSelection = Selection.fromValue(selection);
        } else {
            sharedPreferences.edit().putInt("FILTER_SELECTION", ALL_COMICS.id).apply();
        }
        reloadList(currentSelection);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
            rvList.setVisibility(View.GONE);
        } else {
            pbLoading.setVisibility(View.GONE);
            rvList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        rvList.removeOnScrollListener(rvScrollListener);
        xkcdListPresenter.onDestroy();
        super.onDestroy();
    }

    private boolean lastItemReached() {
        if (mAdapter.getPics() != null) {
            List<XkcdPic> pics = mAdapter.getPics();
            if (pics == null || pics.isEmpty()) {
                return false;
            }
            XkcdPic lastPic = pics.get(pics.size() - 1);
            return xkcdListPresenter.lastItemReached(lastPic.num);
        }
        return false;
    }

    public void showScroller(int visibility) {
        scroller.setVisibility(visibility);
    }

    public void updateData(List<XkcdPic> pics) {
        mAdapter.updateData(pics);
    }

    public void isLoadingMore(boolean loadingMore) {
        this.loadingMore = loadingMore;
    }

    @Override
    public void onItemSelected(int which) {
        if (currentSelection.ordinal() != which) {
            currentSelection = Selection.fromValue(which);
            reloadList(currentSelection);
            switch (currentSelection) {
                case ALL_COMICS:
                    logUXEvent(FIRE_FILTER_ALL);
                    break;
                case MY_FAVORITE:
                    logUXEvent(FIRE_FILTER_FAV);
                    break;
                case PEOPLES_CHOICE:
                    logUXEvent(FIRE_FILTER_THUMB);
                    break;
                default:
                    break;
            }
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
            return ALL_COMICS;
        }
    }
}
