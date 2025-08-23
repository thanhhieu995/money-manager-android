package com.henrystudio.moneymanager.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.ui.calendar.CalendarFragment
import com.henrystudio.moneymanager.ui.calendar.CalendarUpdateFragment
import com.henrystudio.moneymanager.ui.daily.DailyFragment
import com.henrystudio.moneymanager.ui.monthly.MonthlyFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        DailyFragment(),
        CalendarUpdateFragment(),
        MonthlyFragment()
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

    fun getCurrentFragment(position: Int): Fragment {
        return fragments[position]
    }
}

