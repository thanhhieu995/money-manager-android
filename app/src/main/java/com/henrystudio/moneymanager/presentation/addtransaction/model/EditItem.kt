package com.henrystudio.moneymanager.presentation.addtransaction.model

import android.os.Parcelable
import com.henrystudio.moneymanager.data.model.Account
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
sealed class EditItem : Parcelable{
    abstract val id: Int
    abstract val name: String
    @Parcelize
    data class Category(val item: CategoryItem): EditItem() {
        override val id: Int
            get() = item.id
        override val name: String
            get() = item.name
    }
    @Parcelize
    data class AccountItem(val item: Account): EditItem() {
        override val id: Int
            get() = item.id
        override val name: String
            get() = item.name
    }
}
