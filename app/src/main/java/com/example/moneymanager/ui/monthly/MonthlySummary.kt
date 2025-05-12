package com.example.moneymanager.ui.monthly

import java.util.*

data class MonthlySummary(
    val month: String,
    val startDate: Date,
    val endDate: Date,
    val income: Double,
    val expense: Double,
    val total: Double,
    val weeklySummaries: List<WeeklySummary>
)
