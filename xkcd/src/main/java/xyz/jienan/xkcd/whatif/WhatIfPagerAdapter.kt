package xyz.jienan.xkcd.whatif

import androidx.fragment.app.FragmentManager
import xyz.jienan.xkcd.home.base.BaseStatePagerAdapter
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment

class WhatIfPagerAdapter(fm: FragmentManager) : BaseStatePagerAdapter(fm) {

    override fun getItem(position: Int) = SingleWhatIfFragment.newInstance(position + 1)
}