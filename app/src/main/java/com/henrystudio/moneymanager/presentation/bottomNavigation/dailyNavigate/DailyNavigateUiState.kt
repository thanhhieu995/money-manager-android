package com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate

import java.time.LocalDate

data class DailyNavigateUiState(
    val monthLabel: String = "",
    val incomeSum: Long = 0L,
    val expenseSum: Long = 0L,
    val totalSum: Long = 0L,
    val selectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val selectedTotal: Long = 0L,
    val currentTabPosition: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val isYearlyView: Boolean = false
)
