package com.henrystudio.moneymanager.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.henrystudio.moneymanager.presentation.model.TransactionType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val name: String,
    val type: TransactionType,
    val parentId: Int? = null, // null: danh mục cha, != null: danh mục con
    val usageCount: Int = 0,
    val lastUsed: Long = 0L
) : Parcelable