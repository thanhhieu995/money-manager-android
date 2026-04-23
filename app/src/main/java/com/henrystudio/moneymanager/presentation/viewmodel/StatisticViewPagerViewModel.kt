package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
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
class StatisticViewPagerViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticViewPagerUiState())
    val uiState: StateFlow<StatisticViewPagerUiState> = _uiState.asStateFlow()
    private var allTransactions: List<Transaction> = emptyList()

    fun updateFilterOption(filterOption: FilterOption) {
        _uiState.update { it.copy(filterOption = filterOption) }
        calculateTotals()
    }

    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun updateAllTransactions(transactions: List<Transaction>) {
        allTransactions = transactions
        calculateTotals()
    }

    fun updateTabPosition(position: Int) {
        _uiState.update { it.copy(currentTabPosition = position) }
    }

    private fun calculateTotals() {
        val state = _uiState.value
        val totals = transactionUseCases.getStatisticTotalsUseCase(
            transactions = allTransactions,
            filterOption = state.filterOption
        )
        _uiState.update {
            it.copy(
                totalIncome = totals.income,
                totalExpense = totals.expense
            )
        }
    }
}
