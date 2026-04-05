package com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate

data class DailyNavigateUiState(
    val monthLabel: String = "",
    val incomeSum: Double = 0.0,
    val expenseSum: Double = 0.0,
    val totalSum: Double = 0.0,
    val selectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val selectedTotal: Double = 0.0
)
