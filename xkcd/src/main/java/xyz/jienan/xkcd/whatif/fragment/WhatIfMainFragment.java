package xyz.jienan.xkcd.whatif.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.whatif.WhatIfPagerAdapter;
import xyz.jienan.xkcd.whatif.contract.WhatIfMainContract;
import xyz.jienan.xkcd.whatif.presenter.WhatIfMainPresenter;

public class WhatIfMainFragment extends BaseFragment implements WhatIfMainContract.View {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private WhatIfPagerAdapter adapter;

    private WhatIfMainContract.Presenter presenter;

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

    @Override
    public void showFab(WhatIfArticle whatIfArticle) {

    }

    @Override
    public void toggleFab(boolean isFavorite) {

    }

    @Override
    public void showThumbUpCount(Long thumbCount) {

    }
}
