package com.henrystudio.moneymanager.presentation.views.calendar

import com.henrystudio.moneymanager.data.model.TransactionGroup
import java.time.LocalDate

data class CalendarUpdateUiState(
    val monthEvents: Map<LocalDate, TransactionGroup> = emptyMap(),
    val currentDate: LocalDate = LocalDate.now()
)
