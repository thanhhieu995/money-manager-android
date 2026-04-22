package com.henrystudio.moneymanager.presentation.views.search

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.search.FilterPeriodSearch

data class SearchUiState(
    val filteredTransactions: List<Transaction> = emptyList(),
    val incomeTotal: Long = 0L,
    val expenseTotal: Long = 0L,
    val distinctNotes: List<String> = emptyList(),
    val isEmpty: Boolean = true,
    val selectedCount: Int = 0,
    val selectedTotal: Long = 0L,
    val filterPeriod: FilterPeriodSearch = FilterPeriodSearch.All,
    val searchQuery: String = ""
)
