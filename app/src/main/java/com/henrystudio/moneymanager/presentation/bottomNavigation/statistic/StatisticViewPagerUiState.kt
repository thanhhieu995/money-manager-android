package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.time.LocalDate

data class StatisticViewPagerUiState(
    val filterOption: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val currentTabPosition: Int = 0
)
