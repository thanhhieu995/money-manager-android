package com.henrystudio.moneymanager.features.transaction.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction

@Entity(tableName = "transaction_groups")
data class TransactionGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val income: Double,
    val expense: Double,
    val transactions: List<Transaction>
)
