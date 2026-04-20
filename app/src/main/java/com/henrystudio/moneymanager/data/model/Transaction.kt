package com.henrystudio.moneymanager.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val categoryParentId: Int,
    val categoryChildId: Int?,
    val note: String,
    val account: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: Long,
    val isBookmarked: Boolean = false,
    val bookmarkedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long?
) : Parcelable