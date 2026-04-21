package com.henrystudio.moneymanager.presentation.views.search

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.search.FilterPeriodSearch

data class SearchUiState(
    val filteredTransactions: List<Transaction> = emptyList(),
    val incomeTotal: String = "",
    val expenseTotal: String = "",
    val distinctNotes: List<String> = emptyList(),
    val isEmpty: Boolean = true,
    val selectedCount: Int = 0,
    val selectedTotal: String = "",
    val filterPeriod: FilterPeriodSearch = FilterPeriodSearch.All,
    val searchQuery: String = ""
)
