package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.ui.calendar.CalendarFragment
import com.henrystudio.moneymanager.ui.monthly.MonthlyFragment
import com.henrystudio.moneymanager.ui.weekly.WeeklyFragment
import com.henrystudio.moneymanager.ui.yearly.YearlyFragment

class StatisticListTrendAdapter(
    activity: FragmentActivity,
    private val filterOption: FilterOption,
    private val currentFilterPeriod: FilterPeriodStatistic
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Weekly, Monthly, Yearly (hoặc Stats, Account, Note)

    override fun createFragment(position: Int): Fragment {
        val fragment = when (currentFilterPeriod) {
            FilterPeriodStatistic.List -> {
                when (position) {
                    0 -> WeeklyFragment()
                    1 -> MonthlyFragment()
                    2 -> YearlyFragment()
                    else -> MonthlyFragment()
                }
            }
            FilterPeriodStatistic.Trend -> {
                when (position) {
                    0 -> StatisticStatsFragment()
                    1 -> StatisticAccountFragment()
                    2 -> StatisticNoteFragment()
                    else -> StatisticStatsFragment()
                }
            }
            else -> MonthlyFragment()
        }

        // ✅ Truyền filterOptionTemp vào fragment
        fragment.arguments = Bundle().apply {
            putSerializable("filterOption", filterOption)
        }

        return fragment
    }
}