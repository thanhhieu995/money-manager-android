package com.henrystudio.moneymanager.presentation.addtransaction.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AddItemAction: Parcelable {
    object FromAddTransaction : AddItemAction()

    object FromEditCategory: AddItemAction()

    object FromEditAccount: AddItemAction()

    object FromCategoryDetail : AddItemAction() {
        private fun readResolve(): Any = FromCategoryDetail
    }
}