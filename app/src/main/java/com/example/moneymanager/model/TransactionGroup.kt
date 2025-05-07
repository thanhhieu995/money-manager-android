package com.example.moneymanager.model

data class TransactionGroup(
    val date: String,
    val income: Double,
    val expense: Double,
    val transactions: List<Transaction>
)
