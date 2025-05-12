package com.example.moneymanager.ui.monthly

data class WeeklyData(
    val weekRange: String,    // ví dụ: "11-05 ~ 17-05"
    val income: Double,
    val expense: Double,
    val total: Double
)

