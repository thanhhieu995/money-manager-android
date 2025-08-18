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

class StatisticListTrendAdapter(
    activity: FragmentActivity,
    private val categoryType: CategoryType,
    private val filterOption: FilterOption,
    private val currentFilterPeriod: FilterPeriodStatistic
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Weekly, Monthly, Yearly (hoặc Stats, Account, Note)

    @RequiresApi(Build.VERSION_CODES.O)
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
                StatisticCategoryFragment()
            }
            else -> MonthlyFragment()
        }

        // ✅ Truyền filterOptionTemp vào fragment
        fragment.arguments = Bundle().apply {
            when(currentFilterPeriod) {
                FilterPeriodStatistic.Trend -> {
                    // tuỳ vị trí -> truyền filterOption khác nhau
                    val filterOptionTemp = when (position) {
                        0 -> filterOption.copy(type = FilterPeriodStatistic.Weekly)
                        1 -> filterOption.copy(type = FilterPeriodStatistic.Monthly)
                        2 -> filterOption.copy(type = FilterPeriodStatistic.Yearly)
                        else -> filterOption
                    }

                    putSerializable("item_click_statistic_filterOption", filterOptionTemp)

                    // nếu muốn giữ nguyên mấy cái khác thì add luôn
                    putSerializable("item_click_statistic_category_name", Helper.getUpdateMonthText(filterOptionTemp))
                    putSerializable("item_click_statistic_category_type", categoryType)
                    putSerializable("item_click_statistic_keyWord", KeyFilter.Time)
                }
                FilterPeriodStatistic.List -> {
                    putSerializable("filterOption", filterOption)
                }
                else -> {}
            }
        }

        return fragment
    }
}