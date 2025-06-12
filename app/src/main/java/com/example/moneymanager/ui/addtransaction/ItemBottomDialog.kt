package com.example.moneymanager.ui.addtransaction

import com.example.moneymanager.model.Category

data class ItemBottomDialog(
    val emoji: String,
    val name: String
    ) {
    constructor(category: Category) : this(category.emoji, category.name)
}
