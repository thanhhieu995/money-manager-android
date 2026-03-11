package com.henrystudio.moneymanager.presentation.model

import java.time.LocalDate

data class LineChartPoint(
    val label: String,   // ví dụ: "Tuần 1", "Tháng 7", "Ngày 14"
    val amount: Double,
    val date: LocalDate
)
