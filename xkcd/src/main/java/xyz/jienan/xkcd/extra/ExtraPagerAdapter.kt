package xyz.jienan.xkcd.extra

import androidx.fragment.app.Fragment
import xyz.jienan.xkcd.extra.fragment.SingleExtraFragment
import xyz.jienan.xkcd.extra.fragment.SingleExtraWebViewFragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.model.ExtraComics

class ExtraPagerAdapter(fragment: Fragment) : BaseStatePagerAdapter(fragment) {

    private var extraComicsList: List<ExtraComics>? = null

    override fun createFragment(position: Int): Fragment {
        val realPosition = position.coerceIn(0, extraComicsList!!.size - 1)
        val extraComics = extraComicsList!![realPosition]

        return if (extraComics.isWebExtra()) {
            SingleExtraWebViewFragment.newInstance(extraComics)
        } else {
            SingleExtraFragment.newInstance(realPosition + 1)
        }
    }

    fun setEntities(extraComics: List<ExtraComics>?) {
        extraComicsList = extraComics
    }

    override fun getItemCount(): Int = extraComicsList?.size ?: 0

    private fun ExtraComics.isWebExtra(): Boolean {
        return !links?.get(0).isNullOrBlank()
    }
}