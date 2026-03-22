package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AddItemAction: Parcelable {
    object FromAddTransaction : AddItemAction()

    data class FromEditCategory(
        val categoryName: String
    ) : AddItemAction()

    data class FromEditAccount(
        val accountName: String
    ) : AddItemAction()

    object FromCategoryDetail : AddItemAction() {
        private fun readResolve(): Any = FromCategoryDetail
    }
}