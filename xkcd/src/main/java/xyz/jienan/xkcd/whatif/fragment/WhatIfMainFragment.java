package xyz.jienan.xkcd.whatif.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
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

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.whatif.WhatIfPagerAdapter;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;
import xyz.jienan.xkcd.whatif.presenter.WhatIfMainPresenter;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static butterknife.OnPageChange.Callback.PAGE_SELECTED;

public class WhatIfMainFragment extends BaseFragment implements WhatIfMainContract.View {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.btn_fav)
    LikeButton btnFav;

    @BindView(R.id.btn_thumb)
    LikeButton btnThumb;


    private WhatIfPagerAdapter adapter;

    private WhatIfMainContract.Presenter presenter;

    private boolean isFabsShowing = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() != null) {
            final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.menu_whatif);
            }
        }
        adapter = new WhatIfPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        presenter = new WhatIfMainPresenter(this);

        presenter.loadLatestWhatIf();

        RxView.attaches(fab).delay(100, TimeUnit.MILLISECONDS).subscribe(ignored -> fab.hide());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void latestWhatIfLoaded(WhatIfArticle whatIfArticle) {
        adapter.setSize((int) whatIfArticle.num);
        adapter.notifyDataSetChanged();
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SELECTED)
    public void OnPagerSelected(int position) {
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(String.valueOf(position + 1));
        }
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            fab.hide();
            toggleSubFabs(false);
        } else if (state == SCROLL_STATE_IDLE) {
            SingleWhatIfFragment fragment = (SingleWhatIfFragment) adapter.getItemFromMap(viewPager.getCurrentItem() + 1);
            fragment.updateFab();
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

    private void toggleSubFabs(final boolean showSubFabs) {
        btnThumb.setClickable(showSubFabs);
        btnFav.setClickable(showSubFabs);
        ObjectAnimator thumbMove, thumbAlpha, favMove, favAlpha;
        if (showSubFabs) {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, -215);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 1);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -150, -400);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 1);
        } else {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, 0);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 0);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -150);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 0);
        }

        isFabsShowing = showSubFabs;
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(thumbMove, thumbAlpha, favMove, favAlpha);
        animSet.setDuration(300);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (btnThumb != null && btnFav != null && showSubFabs) {
                    btnThumb.setVisibility(View.VISIBLE);
                    btnFav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (btnThumb != null && btnFav != null && !showSubFabs) {
                    btnThumb.setVisibility(View.GONE);
                    btnFav.setVisibility(View.GONE);
                }
            }
        });
        animSet.start();
    }
}
