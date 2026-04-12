package com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate

import java.time.LocalDate

data class DailyNavigateUiState(
    val monthLabel: String = "",
    val incomeSum: Double = 0.0,
    val expenseSum: Double = 0.0,
    val totalSum: Double = 0.0,
    val selectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val selectedTotal: Double = 0.0,
    val currentTabPosition: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val isYearlyView: Boolean = false
)
