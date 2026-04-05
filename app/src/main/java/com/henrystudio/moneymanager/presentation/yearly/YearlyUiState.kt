package com.henrystudio.moneymanager.presentation.views.yearly

data class YearlyUiState(
    val years: List<YearlyData> = emptyList(),
    val isEmpty: Boolean = false
)

