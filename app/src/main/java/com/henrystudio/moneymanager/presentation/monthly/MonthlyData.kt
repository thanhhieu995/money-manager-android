package com.henrystudio.moneymanager.presentation.views.monthly

import java.time.LocalDate

data class MonthlyData(
    val monthName: String,
    val monthStart: LocalDate,
    val dateRange: String,
    val income: Long,
    val expense: Long,
    val total: Long,
    val weeks: List<WeeklyData>,        // Danh sách các tuần
    var isExpanded: Boolean = false     // Trạng thái mở rộng
)
