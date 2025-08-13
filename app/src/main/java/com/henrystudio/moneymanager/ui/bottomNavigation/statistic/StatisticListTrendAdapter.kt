package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.ui.calendar.CalendarFragment
import com.henrystudio.moneymanager.ui.daily.DailyFragment
import com.henrystudio.moneymanager.ui.monthly.MonthlyFragment

class StatisticListTrendAdapter(activity: StatisticListTrendActivity, private val filterPeriodStatistic: FilterPeriodStatistic) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(filterPeriodStatistic) {
            FilterPeriodStatistic.List ->{
                when(position) {
                    0 -> DailyFragment()
                    1 -> MonthlyFragment()
                    2 -> CalendarFragment()
                    else -> MonthlyFragment()
                }
            }
            FilterPeriodStatistic.Trend -> {
                when(position) {
                    0 -> StatisticStatsFragment()
                    1 -> StatisticAccountFragment()
                    2 -> StatisticNoteFragment()
                    else -> StatisticStatsFragment()
                }
            }
            else -> MonthlyFragment()
        }
    }
}