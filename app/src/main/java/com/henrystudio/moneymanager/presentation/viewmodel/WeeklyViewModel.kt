package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.views.monthly.WeeklyData
import com.henrystudio.moneymanager.presentation.views.weekly.WeeklyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class WeeklyViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyUiState())
    val uiState: StateFlow<WeeklyUiState> = _uiState.asStateFlow()

    fun updateData(allTransactionGroups: List<TransactionGroup>, localDate: LocalDate) {
        val listMonthTransactionGroup = FilterTransactions.filterTransactionGroupByMonth(allTransactionGroups, localDate)
        val listWeekData = groupTransactionsByWeek(listMonthTransactionGroup)
        _uiState.update { 
            it.copy(
                weeklyData = listWeekData,
                isEmpty = listWeekData.isEmpty()
            )
        }
    }

    private fun groupTransactionsByWeek(transactions: List<TransactionGroup>): List<WeeklyData> {
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM")

        return transactions
            .groupBy {
                val date = Helper.epochMillisToLocalDate(it.date)
                date.with(DayOfWeek.MONDAY)
            }
            .toSortedMap(compareByDescending { it })
            .map { (weekStart, weekList) ->
                val weekIncome = weekList.sumOf { it.income }
                val weekExpense = weekList.sumOf { it.expense }
                val weekTotal = weekIncome - weekExpense

                WeeklyData(
                    weekStart = weekStart,
                    weekRange = "${weekStart.format(outputFormatter)} ~ ${weekStart.plusDays(6).format(outputFormatter)}",
                    income = weekIncome,
                    expense = weekExpense,
                    total = weekTotal
                )
            }
    }
}
