package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.time.LocalDate

data class StatisticAccountUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val filterOption: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val stats: List<CategoryStat> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isEmpty: Boolean = false
)

