package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.views.calendar.CalendarEventItem
import com.henrystudio.moneymanager.presentation.views.calendar.CalendarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var groupedTransactions: List<TransactionGroup> = emptyList()

    fun updateGroupedTransactions(groups: List<TransactionGroup>) {
        groupedTransactions = groups
        val eventItems = groups.map { group ->
            val dateKey = group.date.substringBefore(" ")
            CalendarEventItem(
                dateKey = dateKey,
                income = group.income,
                expense = group.expense,
                total = group.income - group.expense
            )
        }
        _uiState.update { it.copy(eventItems = eventItems) }
    }

    fun updateCurrentFilterDate(date: LocalDate) {
        _uiState.update { it.copy(currentFilterDate = date) }
    }

    fun getGroupsForDate(dateString: String): List<TransactionGroup> {
        return groupedTransactions.filter { group ->
            group.date.substringBefore(" ") == dateString
        }
    }
}
