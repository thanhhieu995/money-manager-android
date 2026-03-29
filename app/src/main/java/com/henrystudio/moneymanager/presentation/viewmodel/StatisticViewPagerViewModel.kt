package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticViewPagerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class StatisticViewPagerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticViewPagerUiState())
    val uiState: StateFlow<StatisticViewPagerUiState> = _uiState.asStateFlow()

    fun updateFilterOption(filterOption: FilterOption) {
        _uiState.update { it.copy(filterOption = filterOption) }
    }

    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun updateFilteredTransactions(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        _uiState.update {
            it.copy(totalIncome = totalIncome, totalExpense = totalExpense)
        }
    }

    fun updateTabPosition(position: Int) {
        _uiState.update { it.copy(currentTabPosition = position) }
    }
}
