package xyz.jienan.xkcd.extra;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import xyz.jienan.xkcd.extra.fragment.SingleExtraFragment;
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter;

public class ExtraPagerAdapter extends BaseStatePagerAdapter {

    public ExtraPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        SingleExtraFragment fragment = SingleExtraFragment.newInstance(position + 1);
        fragmentsMap.put(position + 1, fragment);
        return fragment;
    }
}
