package com.henrystudio.moneymanager.ui.yearly

import java.time.LocalDate

data class YearlyData(
    val name: Int,
    val date: LocalDate,
    val arrange: String,
    val income: Double,
    val expense: Double,
    val total: Double
)
