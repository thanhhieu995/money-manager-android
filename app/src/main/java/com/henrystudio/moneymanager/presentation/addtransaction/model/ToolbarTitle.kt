package com.henrystudio.moneymanager.presentation.addtransaction.model

sealed class ToolbarTitle {
    object INCOME : ToolbarTitle()
    object EXPENSE : ToolbarTitle()
    object ADD : ToolbarTitle()
    object ACCOUNT: ToolbarTitle()
    object CATEGORY: ToolbarTitle()

    data class Custom(val value: String) : ToolbarTitle()
}