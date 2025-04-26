package com.example.moneymanager.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moneymanager.ui.DailyFragment
import com.example.moneymanager.ui.calendar.CalendarFragment
import com.example.moneymanager.ui.monthly.MonthlyFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DailyFragment()
            1 -> CalendarFragment()
            2 -> MonthlyFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}
