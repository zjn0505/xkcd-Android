package xyz.jienan.xkcd.list.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import xyz.jienan.xkcd.list.WhatIfListAdapter;
import xyz.jienan.xkcd.list.contract.WhatIfListContract;
import xyz.jienan.xkcd.list.presenter.WhatIfListPresenter;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.ui.RecyclerItemClickListener;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_ALL;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_FAV;
import static xyz.jienan.xkcd.Const.FIRE_FILTER_THUMB;
import static xyz.jienan.xkcd.Const.FIRE_LIST_FILTER_BAR;
import static xyz.jienan.xkcd.Const.FIRE_SCROLL_TO_END;
import static xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID;
import static xyz.jienan.xkcd.list.activity.WhatIfListActivity.Selection.ALL_WHAT_IF;

/**
 * Created by jienanzhang on 22/03/2018.
 */

public class WhatIfListActivity extends BaseActivity implements WhatIfListContract.View, ListFilterDialogFragment.OnItemSelectListener {

    @BindView(R.id.rv_list)
    RecyclerView rvList;

    @BindView(R.id.rv_scroller)
    RecyclerViewFastScroller scroller;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private WhatIfListAdapter mAdapter;

    private LinearLayoutManager linearLayoutManager;

    private SharedPreferences sharedPreferences;

    private Selection currentSelection = ALL_WHAT_IF;

    private WhatIfListContract.Presenter whatIfListPresenter;

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
            if (currentSelection != ALL_WHAT_IF) {
                return;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentSelection != ALL_WHAT_IF) {
            outState.putInt("Selection", currentSelection.id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (whatIfListPresenter.hasFav()) {
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
            case ALL_WHAT_IF:
                whatIfListPresenter.loadList();
                break;
            case MY_FAVORITE:
                whatIfListPresenter.loadFavList();
                break;
            case PEOPLES_CHOICE:
                whatIfListPresenter.loadPeopleChoiceList();
                break;
        }
        rvList.scrollToPosition(0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        whatIfListPresenter = new WhatIfListPresenter(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        scroller.setRecyclerView(rvList);
        scroller.setViewsToUse(R.layout.rv_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        mAdapter = new WhatIfListAdapter(this);
        rvList.setAdapter(mAdapter);
        rvList.setHasFixedSize(true);
        rvList.addOnItemTouchListener(new RecyclerItemClickListener(this, rvList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= 0) {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_TARGET_XKCD_ID, position + 1);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // no-ops
            }
        }));
        linearLayoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.addOnScrollListener(rvScrollListener);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            int selection = savedInstanceState.getInt("Selection", ALL_WHAT_IF.id);
            currentSelection = Selection.fromValue(selection);
        } else {
            sharedPreferences.edit().putInt("FILTER_SELECTION", ALL_WHAT_IF.id).apply();
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
        whatIfListPresenter.onDestroy();
        super.onDestroy();
    }

    private boolean lastItemReached() {
        if (mAdapter.getArticles() != null) {
            List<WhatIfArticle> articles = mAdapter.getArticles();
            if (articles == null || articles.isEmpty()) {
                return false;
            }
            WhatIfArticle lastArticle = articles.get(articles.size() - 1);
            return whatIfListPresenter.lastItemReached(lastArticle.num);
        }
        return false;
    }

    public void showScroller(int visibility) {
        scroller.setVisibility(visibility);
    }

    public void updateData(List<WhatIfArticle> articles) {
        mAdapter.updateData(articles);
    }

    @Override
    public void onItemSelected(int which) {
        if (currentSelection.ordinal() != which) {
            currentSelection = Selection.fromValue(which);
            reloadList(currentSelection);
            switch (currentSelection) {
                case ALL_WHAT_IF:
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
        ALL_WHAT_IF(0),
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
            return ALL_WHAT_IF;
        }
    }
}
