package com.henrystudio.moneymanager.presentation.views.addtransaction

import com.henrystudio.moneymanager.presentation.model.AddItemSource

data class AddTransactionToolbarState(
    val title: String = "Transaction",
    val showAddIcon: Boolean = false,
    val showBookmarkIcon: Boolean = true,
    val animation: TitleAnimation? = null,
    val action: AddItemAction? = null
)

sealed class TitleAnimation {
    object SlideFromRight : TitleAnimation()
    object SlideFromLeft : TitleAnimation()
    object None : TitleAnimation()
}
