package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatisticPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StatisticStatsFragment()
            1 -> StatisticAccountFragment()
            2 -> StatisticNoteFragment()
            else -> StatisticStatsFragment()
        }
    }
}
