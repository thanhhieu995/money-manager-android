package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.Note
import com.henrystudio.moneymanager.presentation.model.SortField
import com.henrystudio.moneymanager.presentation.model.SortOrder
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

data class StatisticNoteUiState(
    val notes: List<Note> = emptyList(),
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val filterOption: FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now()),
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val sortField: SortField = SortField.AMOUNT,
    val sortOrder: SortOrder = SortOrder.DESC,
    val isEmpty: Boolean = true
)

@HiltViewModel
class StatisticNoteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticNoteUiState())
    val uiState: StateFlow<StatisticNoteUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
        processData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFilterOption(option: FilterOption) {
        _uiState.update { it.copy(filterOption = option) }
        processData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAllTransactions(transactions: List<Transaction>) {
        _uiState.update { it.copy(allTransactions = transactions) }
        processData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSortFieldClicked(field: SortField) {
        _uiState.update { state ->
            val newOrder = if (state.sortField == field) {
                if (state.sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
            } else {
                SortOrder.DESC
            }
            state.copy(sortField = field, sortOrder = newOrder)
        }
        sortNotes()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processData() {
        val state = _uiState.value
        val filtered = filterTransactions(state.allTransactions, state.filterOption, state.transactionType)
        val notes = calculateNotes(filtered)
        _uiState.update { it.copy(filteredTransactions = filtered, notes = notes, isEmpty = notes.isEmpty()) }
        sortNotes()
    }

    private fun calculateNotes(transactions: List<Transaction>): List<Note> {
        return transactions.groupBy { it.note.trim() }
            .map { (noteText, list) ->
                Note(
                    note = noteText.ifEmpty { "No Note" },
                    count = list.size,
                    amount = list.sumOf { it.amount }
                )
            }
    }

    private fun sortNotes() {
        _uiState.update { state ->
            val sorted = when (state.sortField) {
                SortField.NOTE -> if (state.sortOrder == SortOrder.ASC) state.notes.sortedBy { it.note } else state.notes.sortedByDescending { it.note }
                SortField.COUNT -> if (state.sortOrder == SortOrder.ASC) state.notes.sortedBy { it.count } else state.notes.sortedByDescending { it.count }
                SortField.AMOUNT -> if (state.sortOrder == SortOrder.ASC) state.notes.sortedBy { it.amount } else state.notes.sortedByDescending { it.amount }
            }
            state.copy(notes = sorted)
        }
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
                FilterPeriodStatistic.Monthly -> txDate.month == option.date.month && txDate.year == option.date.year
                FilterPeriodStatistic.Weekly -> {
                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                    val start = option.date.with(weekFields.dayOfWeek(), 1)
                    val end = option.date.with(weekFields.dayOfWeek(), 7)
                    !txDate.isBefore(start) && !txDate.isAfter(end)
                }
                FilterPeriodStatistic.Yearly -> txDate.year == option.date.year
                else -> true
            }
        }
    }
}
