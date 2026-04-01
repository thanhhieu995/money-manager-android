package com.henrystudio.moneymanager.presentation.addtransaction

import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.ToolbarTitle
import com.henrystudio.moneymanager.presentation.model.ItemType

data class AddTransactionToolbarState(
    val title: ToolbarTitle,
    val animation: TitleAnimation,
    val config: ToolbarConfig
)

data class ToolbarConfig(
    val showAdd: Boolean,
    val showBookmark: Boolean,
    val alignTitleToBack: Boolean = false,
    val addAction: AddItemAction? = null,
    val addItemType: ItemType? = null
)

sealed class TitleAnimation {
    object SlideFromRight : TitleAnimation()
    object SlideFromLeft : TitleAnimation()
    object None : TitleAnimation()
}
