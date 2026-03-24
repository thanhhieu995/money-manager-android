package com.henrystudio.moneymanager.presentation.addtransaction.model

import com.henrystudio.moneymanager.data.model.Account
import java.io.Serializable

sealed class EditItem : Serializable{
    abstract val id: Int
    abstract val name: String
    data class Category(val item: CategoryItem): EditItem() {
        override val id: Int
            get() = item.id
        override val name: String
            get() = item.name
    }
    data class AccountItem(val item: Account): EditItem() {
        override val id: Int
            get() = item.id
        override val name: String
            get() = item.name
    }
}
