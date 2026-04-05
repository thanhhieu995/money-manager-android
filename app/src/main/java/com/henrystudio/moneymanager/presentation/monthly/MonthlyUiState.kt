package com.henrystudio.moneymanager.presentation.views.monthly

data class MonthlyUiState(
    val monthlyData: List<MonthlyData> = emptyList(),
    val isEmpty: Boolean = false
)

