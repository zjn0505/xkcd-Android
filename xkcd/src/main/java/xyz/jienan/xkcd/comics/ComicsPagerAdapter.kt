package xyz.jienan.xkcd.comics

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import xyz.jienan.xkcd.comics.fragment.SingleComicFragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter

class ComicsPagerAdapter(fm: FragmentManager) : BaseStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val fragment = SingleComicFragment.newInstance(position + 1)
        fragmentsMap[position + 1] = fragment
        return fragment
    }
}
