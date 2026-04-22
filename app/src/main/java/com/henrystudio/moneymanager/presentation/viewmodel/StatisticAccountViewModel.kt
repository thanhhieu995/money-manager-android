package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import javax.inject.Inject

data class StatisticAccountUiState(
    val stats: List<CategoryStat> = emptyList(),
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val filterOption: FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now()),
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class StatisticAccountViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticAccountUiState())
    val uiState: StateFlow<StatisticAccountUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
        calculateStats()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFilterOption(option: FilterOption) {
        _uiState.update { it.copy(filterOption = option) }
        calculateStats()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAllTransactions(transactions: List<Transaction>) {
        _uiState.update { it.copy(allTransactions = transactions) }
        calculateStats()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateStats() {
        val state = _uiState.value
        val filtered = filterTransactions(state.allTransactions, state.filterOption, state.transactionType)
        val stats = convertToCategoryStats(filtered, state.transactionType == TransactionType.INCOME)
        _uiState.update { it.copy(stats = stats, filteredTransactions = filtered) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterTransactions(
        transactions: List<Transaction>,
        option: FilterOption,
        type: TransactionType
    ): List<Transaction> {
        val isIncome = type == TransactionType.INCOME
        
        return transactions.filter { tx ->
            if (tx.isIncome != isIncome) return@filter false
            
            val txDate = Helper.epochMillisToLocalDate(tx.date)

            when (option.type) {
                FilterPeriodStatistic.Monthly -> {
                    txDate.month == option.date.month && txDate.year == option.date.year
                }
                FilterPeriodStatistic.Weekly -> {
                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                    val start = option.date.with(weekFields.dayOfWeek(), 1)
                    val end = option.date.with(weekFields.dayOfWeek(), 7)
                    !txDate.isBefore(start) && !txDate.isAfter(end)
                }
                FilterPeriodStatistic.Yearly -> {
                    txDate.year == option.date.year
                }
                else -> true
            }
        }
    }

    private fun convertToCategoryStats(transactions: List<Transaction>, isIncome: Boolean): List<CategoryStat> {
        val totalAmount = transactions.sumOf { it.amount }
        if (totalAmount <= 0) return emptyList()

        val colors = listOf(
            android.graphics.Color.parseColor("#FF6F61"),
            android.graphics.Color.parseColor("#6A1B9A"),
            android.graphics.Color.parseColor("#039BE5"),
            android.graphics.Color.parseColor("#43A047"),
            android.graphics.Color.parseColor("#FFB74D"),
            android.graphics.Color.parseColor("#26A69A")
        )

        val grouped = transactions.groupBy { it.account }
        return grouped.entries.mapIndexed { index, entry ->
            val name = entry.key
            val list = entry.value
            val amount = list.sumOf { it.amount }
            CategoryStat(
                name = name,
                amount = amount,
                percent = (amount / totalAmount * 100).toFloat(),
                color = colors[index % colors.size]
            )
        }.sortedByDescending { it.amount }
    }
}
