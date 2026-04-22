package com.henrystudio.moneymanager.presentation.yearly

import com.henrystudio.moneymanager.presentation.views.yearly.YearlyData

data class YearlyUiState(
    val years: List<YearlyData> = emptyList(),
    val isEmpty: Boolean = false
)

