package com.henrystudio.moneymanager.data.model

data class TransactionGroup(
    val id: Int = 0,
    val date: String,
    val income: Double,
    val expense: Double,
    val transactions: List<Transaction>
)
