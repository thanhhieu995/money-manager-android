package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.presentation.statistic.StatisticStatsFragment

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
