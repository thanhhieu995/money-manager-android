package com.example.moneymanager.ui.addtransaction

import com.example.moneymanager.model.Account

sealed class EditItem : java.io.Serializable{
    abstract val id: Int
    data class Category(val item: CategoryItem): EditItem() {
        override val id: Int
            get() = item.id
    }
    data class AccountItem(val item: Account): EditItem() {
        override val id: Int
            get() = item.id
    }
}
