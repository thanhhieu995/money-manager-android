package com.henrystudio.moneymanager.presentation.views.calendar

import java.time.LocalDate

data class CalendarEventItem(
    val dateKey: String,
    val income: Double,
    val expense: Double,
    val total: Double
)

data class CalendarUiState(
    val eventItems: List<CalendarEventItem> = emptyList(),
    val currentFilterDate: LocalDate = LocalDate.now()
)
