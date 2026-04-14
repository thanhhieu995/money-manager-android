package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.views.calendar.CalendarUpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CalendarUpdateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUpdateUiState())
    val uiState: StateFlow<CalendarUpdateUiState> = _uiState.asStateFlow()

    private var groupedTransactions: List<TransactionGroup> = emptyList()

    fun updateData(groups: List<TransactionGroup>, currentDate: LocalDate) {
        groupedTransactions = groups
        val month = currentDate.month
        val year = currentDate.year
        val monthGroups = groups.mapNotNull { group ->
            val localDate =
                Instant.ofEpochMilli(group.date).atZone(ZoneId.systemDefault()).toLocalDate()
            if (localDate.month == month && localDate.year == year) {
                localDate to group
            } else null
        }.toMap()
        _uiState.update {
            it.copy(monthEvents = monthGroups, currentDate = currentDate)
        }
    }

    fun getGroupsForDate(dateEpochMillis: Long): List<TransactionGroup> {
        return groupedTransactions.filter { group ->
            group.date == dateEpochMillis
        }
    }
}
