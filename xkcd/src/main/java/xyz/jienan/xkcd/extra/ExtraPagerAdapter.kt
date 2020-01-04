package xyz.jienan.xkcd.extra

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import xyz.jienan.xkcd.extra.fragment.SingleExtraFragment
import xyz.jienan.xkcd.extra.fragment.SingleExtraWebViewFragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.model.ExtraComics

class ExtraPagerAdapter(fm: FragmentManager) : BaseStatePagerAdapter(fm) {

    private var extraComicsList: List<ExtraComics>? = null

    override fun getItem(position: Int): Fragment {
        var realPosition = position
        if (realPosition > extraComicsList!!.size) {
            realPosition = extraComicsList!!.size - 1
        }
        val extraComics = extraComicsList!![realPosition]

        return if (!extraComics.links?.get(0).isNullOrBlank()) {
            SingleExtraWebViewFragment.newInstance(extraComics)
        } else {
            SingleExtraFragment.newInstance(realPosition + 1)
        }
    }

    fun setEntities(extraComics: List<ExtraComics>?) {
        extraComicsList = extraComics
    }
}