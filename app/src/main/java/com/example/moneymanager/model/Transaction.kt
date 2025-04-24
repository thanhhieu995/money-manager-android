package com.example.moneymanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val content: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: String
)