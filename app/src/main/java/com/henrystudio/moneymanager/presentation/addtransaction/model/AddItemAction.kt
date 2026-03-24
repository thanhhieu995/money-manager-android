package com.henrystudio.moneymanager.presentation.addtransaction.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AddItemAction: Parcelable {
    object FromAddTransaction : AddItemAction()

    data class FromEditCategory(
        val item: CategoryItem
    ) : AddItemAction()

    data class FromEditAccount(
        val accountName: String
    ) : AddItemAction()

    object FromCategoryDetail : AddItemAction() {
        private fun readResolve(): Any = FromCategoryDetail
    }
}