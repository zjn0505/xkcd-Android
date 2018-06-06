package xyz.jienan.xkcd.whatif;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import xyz.jienan.xkcd.base.BaseStatePagerAdapter;
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment;

public class WhatIfPagerAdapter extends BaseStatePagerAdapter {

    public WhatIfPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        SingleWhatIfFragment fragment = SingleWhatIfFragment.newInstance(position + 1);
        fragmentsMap.put(position + 1, fragment);
        return fragment;
    }
}
