package com.henrystudio.moneymanager.presentation.daily.model

data class DailyTransactionGroupUi(
    val id: Int,
    val date: String,
    val income: Double,
    val expense: Double,
    val transactions: List<DailyTransactionUi>
)
