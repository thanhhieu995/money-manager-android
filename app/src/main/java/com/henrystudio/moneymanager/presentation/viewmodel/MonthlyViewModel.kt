package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyData
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyUiState
import com.henrystudio.moneymanager.presentation.views.monthly.WeeklyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Screen-specific state holder for the Monthly screen.
 *
 * Consumes grouped transactions (typically from SharedTransactionViewModel),
 * transforms them into MonthlyData/WeeklyData and exposes a MonthlyUiState
 * backed by StateFlow for the UI to observe.
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MonthlyViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyUiState())
    val uiState: StateFlow<MonthlyUiState> = _uiState

    fun updateMonthlyData(
        groups: List<TransactionGroup>,
        anchorDate: LocalDate
    ) {
        val filterTransactionYear = FilterTransactions.filterTransactionGroupByYear(
            groups,
            anchorDate
        )
        val monthlyData = groupTransactionsByMonth(filterTransactionYear)
        _uiState.value = MonthlyUiState(
            monthlyData = monthlyData,
            isEmpty = monthlyData.isEmpty()
        )
    }

    private fun groupTransactionsByMonth(transactions: List<TransactionGroup>): List<MonthlyData> {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM")

        return transactions
            .groupBy { group ->
                val rawDate = group.date
                val cleanedDate = rawDate.substringBefore(" ")
                val localDate = LocalDate.parse(cleanedDate, inputFormatter)
                LocalDate.of(localDate.year, localDate.month, 1)
            }
            .map { (monthStart, monthList) ->
                val income = monthList.sumOf { it.income }
                val expense = monthList.sumOf { it.expense }
                val total = income - expense

                val dateRange =
                    "${monthStart.format(outputFormatter)} ~ ${
                        monthStart.withDayOfMonth(
                            monthStart.lengthOfMonth()
                        ).format(outputFormatter)
                    }"

                val weeklyGroups = monthList.groupBy { group ->
                    val rawDate = group.date
                    val cleanedDate = rawDate.substringBefore(" ")
                    val date = LocalDate.parse(cleanedDate, inputFormatter)
                    date.with(DayOfWeek.MONDAY)
                }

                val weeks = weeklyGroups
                    .toSortedMap(compareByDescending { it })
                    .map { (weekStart, weekList) ->
                        val weekIncome = weekList.sumOf { it.income }
                        val weekExpense = weekList.sumOf { it.expense }
                        val weekTotal = weekIncome - weekExpense

                        WeeklyData(
                            weekStart = weekStart,
                            weekRange = "${weekStart.format(outputFormatter)} ~ ${
                                weekStart.plusDays(6).format(outputFormatter)
                            }",
                            income = weekIncome,
                            expense = weekExpense,
                            total = weekTotal
                        )
                    }

                val monthFormatter =
                    DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())

                MonthlyData(
                    monthName = monthStart.format(monthFormatter),
                    monthStart = monthStart,
                    dateRange = dateRange,
                    income = income,
                    expense = expense,
                    total = total,
                    weeks = weeks
                )
            }
    }
}

