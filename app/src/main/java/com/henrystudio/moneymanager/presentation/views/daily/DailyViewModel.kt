package com.henrystudio.moneymanager.presentation.views.daily

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailyViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    fun updateData(
        transactions: List<TransactionGroup>,
        filterOption: FilterOption,
        selectedMonth: LocalDate,
        categoryName: String?,
        transactionType: TransactionType,
        keyFilter: KeyFilter,
        isFromMainActivity: Boolean
    ) {
        val filteredByCategory = if (categoryName != null) {
            filterByCategory(transactions, categoryName, transactionType, keyFilter)
        } else {
            transactions
        }

        val firstDayOfMonth = LocalDate.of(selectedMonth.year, selectedMonth.month, 1)
        val lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())

        val finalFilteredList = if (isFromMainActivity) {
            FilterTransactions.Companion.filterTransactionGroupByMonth(filteredByCategory, lastDayOfMonth)
        } else {
            when (filterOption.type) {
                FilterPeriodStatistic.Weekly -> {
                    FilterTransactions.Companion.filterTransactionGroupByWeek(filteredByCategory, selectedMonth)
                }
                FilterPeriodStatistic.Monthly -> {
                    FilterTransactions.Companion.filterTransactionGroupByMonth(filteredByCategory, lastDayOfMonth)
                }
                FilterPeriodStatistic.Yearly -> {
                    FilterTransactions.Companion.filterTransactionGroupByYear(filteredByCategory, lastDayOfMonth)
                }
                else -> {
                    FilterTransactions.Companion.filterTransactionGroupByMonth(filteredByCategory, lastDayOfMonth)
                }
            }
        }

        // ❌ ignore empty lần đầu
        if (finalFilteredList.isEmpty()) {
            _uiState.update {
                it.copy(dataTransactionGroupState = UiState.Loading)
            }
            return
        }

        val newState = UiState.Success(finalFilteredList)

        _uiState.update {
            it.copy(
                dataTransactionGroupState = newState,
                transactions = finalFilteredList,
                selectedDate = selectedMonth,
                isEmpty = finalFilteredList.isEmpty(),
                isYearly = filterOption.type == FilterPeriodStatistic.Yearly,
            )
        }
    }

    private fun filterByCategory(
        transactions: List<TransactionGroup>,
        categoryName: String,
        transactionType: TransactionType,
        keyFilter: KeyFilter
    ): List<TransactionGroup> {
        val isInCome = transactionType == TransactionType.INCOME
        return transactions.mapNotNull { group ->
            val filteredTransactions = when (keyFilter) {
                KeyFilter.CategoryParent -> {
                    group.transactions.filter {
                        it.categoryParentName.equals(categoryName, ignoreCase = true)
                    }
                }
                KeyFilter.CategorySub -> {
                    group.transactions.filter {
                        it.categorySubName.trim().equals(categoryName.trim(), ignoreCase = true)
                    }
                }
                KeyFilter.Note -> {
                    group.transactions.filter {
                        it.note.trim().equals(categoryName.trim(), ignoreCase = true) && it.isIncome == isInCome
                    }
                }
                KeyFilter.Account -> {
                    group.transactions.filter {
                        it.account.trim().equals(categoryName.trim(), ignoreCase = true)
                    }
                }
                else -> group.transactions.filter { it.isIncome == isInCome }
            }

            if (filteredTransactions.isNotEmpty()) {
                group.copy(
                    income = filteredTransactions.filter { it.isIncome }.sumOf { it.amount },
                    expense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount },
                    transactions = filteredTransactions
                )
            } else null
        }
    }

    fun updateSelection(
        selectionMode: Boolean,
        selectedTransactions: List<Transaction>
    ) {
        _uiState.update {
            it.copy(
                selectionMode = selectionMode,
                selectedTransactions = selectedTransactions
            )
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setLoading(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Loading,
                selectedDate = selectedMonth
            )
        }
    }

    fun setEmpty(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Empty,
                selectedDate = selectedMonth
            )
        }
    }
}