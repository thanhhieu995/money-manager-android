package com.example.moneymanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val name: String,
    val type: CategoryType
)

enum class CategoryType {
    INCOME, EXPENSE
}
