package xyz.jienan.xkcd.comics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.HashMap;

import xyz.jienan.xkcd.home.fragment.SingleComicFragment;

public class ComicsPagerAdapter extends FragmentStatePagerAdapter {

    private int length;

    private HashMap<Integer, SingleComicFragment> fragmentsMap = new HashMap<>();

    public ComicsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setSize(int size) {
        length = size;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        SingleComicFragment fragment = SingleComicFragment.newInstance(position + 1);
        fragmentsMap.put(position + 1, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragmentsMap.remove(position + 1);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return length;
    }
}
