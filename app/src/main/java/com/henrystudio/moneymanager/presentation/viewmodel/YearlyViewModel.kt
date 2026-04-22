package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.views.yearly.YearlyData
import com.henrystudio.moneymanager.presentation.yearly.YearlyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class YearlyViewModel @Inject constructor(
    transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(YearlyUiState())
    val uiState: StateFlow<YearlyUiState> = _uiState.asStateFlow()

    private val allTransactions: StateFlow<List<Transaction>> =
        transactionUseCases.getTransactionsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            allTransactions.collect { transactions ->
                val years = mapTransactionsToYearlyData(transactions)
                _uiState.value = YearlyUiState(
                    years = years,
                    isEmpty = years.isEmpty()
                )
            }
        }
    }

    private fun mapTransactionsToYearlyData(transactions: List<Transaction>): List<YearlyData> {
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return transactions
            .groupBy { Helper.epochMillisToLocalDate(it.date).year }
            .map { (year, yearTransactions) ->
                val income = yearTransactions.filter { it.isIncome }.sumOf { it.amount }
                val expense = yearTransactions.filter { !it.isIncome }.sumOf { it.amount }

                val startDate = LocalDate.of(year, 1, 1).format(outputFormatter)
                val endDate = LocalDate.of(year, 12, 31).format(outputFormatter)

                YearlyData(
                    name = year,
                    date = LocalDate.of(year, 1, 1),
                    arrange = "$startDate ~ $endDate",
                    income = income,
                    expense = expense,
                    total = income - expense
                )
            }
            .sortedByDescending { it.name }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilterForYearSelection(data: YearlyData): Pair<String, FilterOption> {
        val filterDateStr = Helper.formatDateFromFilterOptionToDateDaily(data.date.toString())
        val filterOption = FilterOption(FilterPeriodStatistic.Yearly, data.date)
        return filterDateStr to filterOption
    }
}

