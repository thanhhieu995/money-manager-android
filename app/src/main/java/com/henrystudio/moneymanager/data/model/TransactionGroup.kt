package com.henrystudio.moneymanager.data.model

data class TransactionGroup(
    val id: Int = 0,
    val date: Long,
    val income: Long,
    val expense: Long,
    val transactions: List<Transaction>
)
