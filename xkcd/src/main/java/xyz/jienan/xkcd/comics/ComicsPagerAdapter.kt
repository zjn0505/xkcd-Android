package xyz.jienan.xkcd.comics

import androidx.fragment.app.Fragment
import xyz.jienan.xkcd.comics.fragment.SingleComicFragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter

class ComicsPagerAdapter(fragment: Fragment) : BaseStatePagerAdapter(fragment) {

    override fun createFragment(position: Int)= SingleComicFragment.newInstance(position + 1)
}
