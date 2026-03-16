package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import java.time.LocalDate

data class StatisticViewPagerUiState(
    val filterOption: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currentTabPosition: Int = 0
)
