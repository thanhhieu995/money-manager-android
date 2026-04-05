package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.Note
import com.henrystudio.moneymanager.presentation.model.SortField
import com.henrystudio.moneymanager.presentation.model.SortOrder
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.time.LocalDate

data class StatisticNoteUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val filterOption: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val notes: List<Note> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val sortField: SortField = SortField.AMOUNT,
    val sortOrder: SortOrder = SortOrder.DESC,
    val isEmpty: Boolean = false
)
