package com.example.moneymanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val name: String,
    val type: CategoryType,
    val parentId: Int? = null // null: danh mục cha, != null: danh mục con
) : java.io.Serializable

enum class CategoryType : java.io.Serializable {
    INCOME, EXPENSE
}
