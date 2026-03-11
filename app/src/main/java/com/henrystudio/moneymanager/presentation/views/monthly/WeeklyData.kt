package com.henrystudio.moneymanager.presentation.views.monthly

import java.time.LocalDate

data class WeeklyData(
    val weekStart: LocalDate,
    val weekRange: String,    // ví dụ: "11-05 ~ 17-05"
    val income: Double,
    val expense: Double,
    val total: Double
)

