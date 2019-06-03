package xyz.jienan.xkcd.whatif;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter;
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment;

public class WhatIfPagerAdapter extends BaseStatePagerAdapter {

    public WhatIfPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        SingleWhatIfFragment fragment = SingleWhatIfFragment.Companion.newInstance(position + 1);
        fragmentsMap.put(position + 1, fragment);
        return fragment;
    }
}
