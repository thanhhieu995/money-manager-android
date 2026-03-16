package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.DailyNavigateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailyNavigateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DailyNavigateUiState())
    val uiState: StateFlow<DailyNavigateUiState> = _uiState.asStateFlow()

    fun updateFrom(
        groups: List<TransactionGroup>,
        date: LocalDate,
        tabPosition: Int,
        selectionMode: Boolean,
        selectedTransactions: List<Transaction>
    ) {
        val isMonthlyTab = tabPosition == 2
        val filtered = if (isMonthlyTab) {
            FilterTransactions.filterTransactionGroupByYear(groups, date)
        } else {
            FilterTransactions.filterTransactionGroupByMonth(groups, date)
        }
        val incomeSum = filtered.sumOf { it.income }
        val expenseSum = filtered.sumOf { it.expense }
        val totalSum = incomeSum - expenseSum
        val locale = Locale.getDefault()
        val monthLabel = if (isMonthlyTab) {
            date.format(DateTimeFormatter.ofPattern("yyyy", locale))
        } else {
            date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
        }
        val selectedTotal = selectedTransactions.sumOf { if (it.isIncome) it.amount else -it.amount }
        _uiState.value = DailyNavigateUiState(
            monthLabel = monthLabel,
            incomeSum = incomeSum,
            expenseSum = expenseSum,
            totalSum = totalSum,
            selectionMode = selectionMode,
            selectedCount = selectedTransactions.size,
            selectedTotal = selectedTotal
        )
    }
}
