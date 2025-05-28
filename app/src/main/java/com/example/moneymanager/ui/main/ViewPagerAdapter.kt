package com.example.moneymanager.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moneymanager.ui.daily.DailyFragment
import com.example.moneymanager.ui.calendar.CalendarFragment
import com.example.moneymanager.ui.monthly.MonthlyFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        DailyFragment(),
        CalendarFragment(),
        MonthlyFragment()
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

    fun getCurrentFragment(position: Int): Fragment {
        return fragments[position]
    }
}

