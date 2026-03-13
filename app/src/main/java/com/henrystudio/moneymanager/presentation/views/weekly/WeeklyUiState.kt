package com.henrystudio.moneymanager.presentation.views.weekly

import com.henrystudio.moneymanager.presentation.views.monthly.WeeklyData

data class WeeklyUiState(
    val weeklyData: List<WeeklyData> = emptyList(),
    val isEmpty: Boolean = true
)