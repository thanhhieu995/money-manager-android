package com.henrystudio.moneymanager.presentation.addtransaction.model

import android.os.Parcelable
import com.henrystudio.moneymanager.presentation.model.ItemType
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