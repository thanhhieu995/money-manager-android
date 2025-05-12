package com.example.moneymanager.ui.monthly

import java.util.*

data class WeeklySummary(
    val startDate: Date,
    val endDate: Date,
    val income: Double,
    val expense: Double,
    val total: Double
)
