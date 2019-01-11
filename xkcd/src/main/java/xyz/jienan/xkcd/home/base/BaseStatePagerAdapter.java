package xyz.jienan.xkcd.home.base;

import android.view.ViewGroup;

import java.util.HashMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public abstract class BaseStatePagerAdapter extends FragmentStatePagerAdapter {

    protected HashMap<Integer, Fragment> fragmentsMap = new HashMap<>();
    private int length;

    public BaseStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setSize(int size) {
        length = size;
        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragmentsMap.remove(position + 1);
        super.destroyItem(container, position, object);
    }

    public Fragment getItemFromMap(int position) {
        return fragmentsMap.get(position);
    }

    @Override
    public int getCount() {
        return length;
    }
}
