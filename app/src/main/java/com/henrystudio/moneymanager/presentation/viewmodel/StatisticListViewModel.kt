package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

data class StatisticListUiState(
    val filterOption: FilterOption? = null,
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val currentFilterPeriod: FilterPeriodStatistic? = null,
    val monthLabel: String = "",
    val incomeSum: String = "0đ",
    val expenseSum: String = "0đ",
    val totalSum: String = "0đ",
    val showBack: Boolean = true,
    val showNext: Boolean = true,
    val showSummary: Boolean = true
)

@HiltViewModel
class StatisticListViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticListUiState())
    val uiState: StateFlow<StatisticListUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun setArgs(filterOption: FilterOption, categoryType: CategoryType, currentFilterPeriod: FilterPeriodStatistic) {
        _uiState.update { it.copy(
            filterOption = filterOption,
            categoryType = categoryType,
            currentFilterPeriod = currentFilterPeriod,
            showBack = currentFilterPeriod != FilterPeriodStatistic.Trend,
            showNext = currentFilterPeriod != FilterPeriodStatistic.Trend,
            showSummary = currentFilterPeriod != FilterPeriodStatistic.Trend
        ) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTabSelected(position: Int) {
        val currentOption = _uiState.value.filterOption ?: return
        val newType = when (position) {
            0 -> FilterPeriodStatistic.Weekly
            1 -> FilterPeriodStatistic.Monthly
            2 -> FilterPeriodStatistic.Yearly
            else -> FilterPeriodStatistic.Monthly
        }
        _uiState.update { it.copy(filterOption = currentOption.copy(type = newType)) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateCurrentDate(date: LocalDate) {
        _uiState.update { state ->
            val newOption = state.filterOption?.copy(date = date)
            state.copy(
                filterOption = newOption,
                monthLabel = newOption?.let { Helper.getUpdateMonthText(it) } ?: ""
            )
        }
    }

    fun updateGroups(groups: List<TransactionGroup>) {
        val totalIncome = groups.sumOf { group -> group.transactions.filter { it.isIncome }.sumOf { it.amount } }
        val totalExpense = groups.sumOf { group -> group.transactions.filter { !it.isIncome }.sumOf { it.amount } }
        val total = totalIncome - totalExpense
        
        _uiState.update { it.copy(
            incomeSum = Helper.formatCurrency(totalIncome),
            expenseSum = Helper.formatCurrency(totalExpense),
            totalSum = Helper.formatCurrency(total)
        ) }
    }

    fun getTabPosition(type: FilterPeriodStatistic): Int {
        return when (type) {
            FilterPeriodStatistic.Weekly -> 0
            FilterPeriodStatistic.Monthly -> 1
            FilterPeriodStatistic.Yearly -> 2
            else -> 1
        }
    }

    fun getFilterOption() = _uiState.value.filterOption
}
