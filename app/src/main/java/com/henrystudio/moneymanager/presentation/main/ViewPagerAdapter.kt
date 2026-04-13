package com.henrystudio.moneymanager.presentation.views.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.presentation.views.calendar.CalendarUpdateFragment
import com.henrystudio.moneymanager.presentation.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DailyFragment()
            1 -> CalendarUpdateFragment()
            else -> MonthlyFragment()
        }
    }
}
