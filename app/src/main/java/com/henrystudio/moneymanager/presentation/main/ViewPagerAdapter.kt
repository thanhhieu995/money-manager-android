package com.henrystudio.moneymanager.presentation.views.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.presentation.views.calendar.CalendarUpdateFragment
import com.henrystudio.moneymanager.presentation.views.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyFragment

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

