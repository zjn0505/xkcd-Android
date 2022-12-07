package xyz.jienan.xkcd.home.base

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


abstract class BaseStatePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var size = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = size
}