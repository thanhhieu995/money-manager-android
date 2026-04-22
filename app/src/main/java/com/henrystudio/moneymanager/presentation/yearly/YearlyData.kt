package com.henrystudio.moneymanager.presentation.views.yearly

import java.time.LocalDate

data class YearlyData(
    val name: Int,
    val date: LocalDate,
    val arrange: String,
    val income: Long,
    val expense: Long,
    val total: Long
)
