package xyz.jienan.xkcd.comics

import androidx.fragment.app.FragmentManager
import xyz.jienan.xkcd.comics.fragment.SingleComicFragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter

class ComicsPagerAdapter(fm: FragmentManager) : BaseStatePagerAdapter(fm) {

    override fun getItem(position: Int) = SingleComicFragment.newInstance(position + 1)
}
