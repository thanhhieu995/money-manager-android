package com.henrystudio.moneymanager.data.model

import android.os.Parcel
import android.os.Parcelable
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

enum class CategoryType : Parcelable {
    INCOME {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(p0: Parcel, p1: Int) {
            TODO("Not yet implemented")
        }
    }, EXPENSE {
        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(p0: Parcel, p1: Int) {
            TODO("Not yet implemented")
        }
    }
}
