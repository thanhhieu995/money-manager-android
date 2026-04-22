package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.core.util.Helper
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


    fun updateCurrentFilterDate(date: LocalDate) {
        _uiState.update { it.copy(currentFilterDate = date) }
    }

    fun getGroupsForDate(dateEpochMillis: Long): List<TransactionGroup> {
        return groupedTransactions.filter { group -> group.date == dateEpochMillis }
    }
}
