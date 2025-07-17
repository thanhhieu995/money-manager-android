package com.henrystudio.moneymanager.ui.monthly

data class MonthlyData(
    val monthName: String,
    val dateRange: String,
    val income: Double,
    val expense: Double,
    val total: Double,
    val weeks: List<WeeklyData>,        // Danh sách các tuần
    var isExpanded: Boolean = false     // Trạng thái mở rộng
)
