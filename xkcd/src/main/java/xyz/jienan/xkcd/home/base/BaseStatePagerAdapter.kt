package xyz.jienan.xkcd.home.base

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

abstract class BaseStatePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var size = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = size
}