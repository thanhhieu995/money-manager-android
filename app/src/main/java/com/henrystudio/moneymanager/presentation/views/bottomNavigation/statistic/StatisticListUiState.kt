package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic

data class StatisticListUiState(
    val filterOption: FilterOption? = null,
    val categoryType: CategoryType? = null,
    val currentFilterPeriod: FilterPeriodStatistic? = null,
    val monthLabel: String = "",
    val incomeSum: String = "",
    val expenseSum: String = "",
    val totalSum: String = "",
    val showBack: Boolean = true,
    val showNext: Boolean = true,
    val showSummary: Boolean = true,
    val layoutControlVisible: Boolean = true,
    val tabPosition: Int = 1
)
