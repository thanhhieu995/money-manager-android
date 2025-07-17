package com.henrystudio.moneymanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val note: String,
    val account: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: String,
    val isBookmarked: Boolean = false
) : java.io.Serializable