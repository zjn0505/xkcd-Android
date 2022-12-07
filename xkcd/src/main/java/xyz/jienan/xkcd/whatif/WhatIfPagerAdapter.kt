package xyz.jienan.xkcd.whatif

import androidx.fragment.app.Fragment
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment

class WhatIfPagerAdapter(fragment: Fragment) : BaseStatePagerAdapter(fragment) {

    override fun createFragment(position: Int) = SingleWhatIfFragment.newInstance(position + 1)
}