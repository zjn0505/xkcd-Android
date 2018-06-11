package xyz.jienan.xkcd.comics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter;
import xyz.jienan.xkcd.comics.fragment.SingleComicFragment;

public class ComicsPagerAdapter extends BaseStatePagerAdapter {


    public ComicsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        SingleComicFragment fragment = SingleComicFragment.newInstance(position + 1);
        fragmentsMap.put(position + 1, fragment);
        return fragment;
    }
}
