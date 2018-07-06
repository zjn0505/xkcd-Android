package xyz.jienan.xkcd.whatif.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindString;
import butterknife.OnPageChange;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment;
import xyz.jienan.xkcd.list.activity.WhatIfListActivity;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.whatif.WhatIfPagerAdapter;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;
import xyz.jienan.xkcd.whatif.presenter.WhatIfMainPresenter;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.FIRE_WHAT_IF_SUFFIX;
import static xyz.jienan.xkcd.Const.LAST_VIEW_WHAT_IF_ID;

public class WhatIfMainFragment extends ContentMainBaseFragment implements WhatIfMainContract.View {


    @BindString(R.string.menu_whatif)
    String titleText;

    @BindString(R.string.search_hint_what_if)
    String searchHint;

    private List<WhatIfArticle> searchSuggestions;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_main;
    }

    @Override
    protected void suggestionClicked(int position) {
        WhatIfArticle article = searchSuggestions.get(position);
        scrollViewPagerToItem((int) (article.num - 1), false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = new WhatIfPagerAdapter(getChildFragmentManager());
        presenter = new WhatIfMainPresenter(this);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RxView.attaches(fab)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> fab.hide(),
                        e -> Timber.e(e, "fab observing error"));
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0) {
            outState.putInt(LAST_VIEW_WHAT_IF_ID, viewPager.getCurrentItem() + 1);
        }
    }

    @Override
    protected String getTitleTextRes() {
        return titleText;
    }

    @Override
    protected int getPickerTitleTextRes() {
        return R.string.dialog_pick_content_what_if;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_what_if, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_what_if_list:
                Intent intent = new Intent(getActivity(), WhatIfListActivity.class);
                startActivityForResult(intent, REQ_LIST_ACTIVITY);
                logUXEvent(FIRE_BROWSE_LIST_MENU + FIRE_WHAT_IF_SUFFIX);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void latestWhatIfLoaded(WhatIfArticle whatIfArticle) {
        latestIndex = (int) whatIfArticle.num;
        super.latestLoaded();
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE) {
            SingleWhatIfFragment fragment = (SingleWhatIfFragment) adapter.getItemFromMap(viewPager.getCurrentItem() + 1);
            if (fragment != null) {
                fragment.updateFab();
            }
        }
    }

    @Override
    public void showFab(WhatIfArticle whatIfArticle) {
        toggleFab(whatIfArticle.isFavorite);
        btnFav.setLiked(whatIfArticle.isFavorite);
        btnThumb.setLiked(whatIfArticle.hasThumbed);
        fab.show();
    }

    @Override
    public void toggleFab(boolean isFavorite) {
        if (isFavorite) {
            fabAnimation(R.color.pink, R.color.white, R.drawable.ic_heart_on);
        } else {
            fabAnimation(R.color.white, R.color.pink, R.drawable.ic_heart_white);
        }
    }

    @Override
    public void showThumbUpCount(Long thumbCount) {
        showToast(getContext(), String.valueOf(thumbCount));
    }

    @Override
    public void renderWhatIfSearch(List<WhatIfArticle> articles) {
        searchSuggestions = articles;
        String[] columns = {BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };
        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < searchSuggestions.size(); i++) {
            WhatIfArticle article = searchSuggestions.get(i);
            String[] tmp = {Integer.toString(i), article.featureImg, article.title, String.valueOf(article.num)};
            cursor.addRow(tmp);
        }
        searchAdapter.swapCursor(cursor);
    }

    @Override
    protected CharSequence getSearchHint() {
        return searchHint;
    }

    void showOrHideFabWithInfo(boolean isShowing) {
        if (isShowing) {
            presenter.getInfoAndShowFab(getCurrentIndex());
        } else {
            fab.hide();
            toggleSubFabs(false);
        }
    }
}
