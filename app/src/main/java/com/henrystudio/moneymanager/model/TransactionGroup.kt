package com.henrystudio.moneymanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_groups")
data class TransactionGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val income: Double,
    val expense: Double,
    val transactions: List<Transaction>
)
