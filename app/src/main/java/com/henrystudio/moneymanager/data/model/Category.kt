package com.henrystudio.moneymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.io.Serializable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val name: String,
    val type: TransactionType,
    val parentId: Int? = null // null: danh mục cha, != null: danh mục con
) : Serializable

enum class CategoryType : Serializable {
    INCOME, EXPENSE
}
