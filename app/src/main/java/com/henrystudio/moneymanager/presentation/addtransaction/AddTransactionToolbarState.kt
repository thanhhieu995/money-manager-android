package com.henrystudio.moneymanager.presentation.addtransaction

import com.henrystudio.moneymanager.presentation.addtransaction.model.ToolbarTitle

data class AddTransactionToolbarState(
    val title: ToolbarTitle,
    val animation: TitleAnimation,
    val config: ToolbarConfig
)

data class ToolbarConfig(
    val showAdd: Boolean,
    val showBookmark: Boolean,
    val alignTitleToBack: Boolean = false
)

sealed class TitleAnimation {
    object SlideFromRight : TitleAnimation()
    object SlideFromLeft : TitleAnimation()
    object None : TitleAnimation()
}
