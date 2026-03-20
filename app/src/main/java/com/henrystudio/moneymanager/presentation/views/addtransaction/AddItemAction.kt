package com.henrystudio.moneymanager.presentation.views.addtransaction

import com.henrystudio.moneymanager.presentation.model.ItemType

sealed class AddItemAction {
    data class FromAddTransaction(val itemType: ItemType) : AddItemAction()

    data class FromEditCategory(
        val categoryName: String
    ) : AddItemAction()

    data class FromEditAccount(
        val accountName: String
    ) : AddItemAction()

    object FromCategoryDetail : AddItemAction()
}