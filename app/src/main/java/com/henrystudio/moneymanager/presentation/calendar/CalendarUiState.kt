package com.henrystudio.moneymanager.presentation.views.calendar

import java.time.LocalDate

data class CalendarEventItem(
    val dateKey: LocalDate,
    val income: Long,
    val expense: Long,
    val total: Long
)

data class CalendarUiState(
    val eventItems: List<CalendarEventItem> = emptyList(),
    val currentFilterDate: LocalDate = LocalDate.now()
)
