package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.CategoryType
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.model.KeyFilter
import com.henrystudio.moneymanager.ui.monthly.MonthlyFragment
import com.henrystudio.moneymanager.ui.weekly.WeeklyFragment
import com.henrystudio.moneymanager.ui.yearly.YearlyFragment

@RequiresApi(Build.VERSION_CODES.O)
class StatisticListTrendAdapter(
    activity: FragmentActivity,
    private var filterOption: FilterOption
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WeeklyFragment()
            1 -> MonthlyFragment()
            2 -> YearlyFragment()
            else -> MonthlyFragment()
        }.apply {
            arguments = Bundle().apply {
                putSerializable("filterOption", filterOption)
            }
        }
    }
}