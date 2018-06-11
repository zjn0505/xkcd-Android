package xyz.jienan.xkcd.whatif.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.whatif.WhatIfPagerAdapter;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;
import xyz.jienan.xkcd.whatif.presenter.WhatIfMainPresenter;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static butterknife.OnPageChange.Callback.PAGE_SELECTED;

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
        RxView.attaches(fab).delay(100, TimeUnit.MILLISECONDS).subscribe(ignored -> fab.hide());
        return view;
    }

    @Override
    protected String getTitleTextRes() {
        return titleText;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_what_if, menu);
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
//            comicsMainPresenter.getInfoAndShowFab(getCurrentIndex());
        }
    }

    @Override
    public void showFab(WhatIfArticle whatIfArticle) {

    }

    @Override
    public void toggleFab(boolean isFavorite) {

    }

    @Override
    public void showThumbUpCount(Long thumbCount) {

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
}
