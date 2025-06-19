package com.example.moneymanager.ui.addtransaction

data class CategoryItem(
    val id: Int,
    val emoji: String,
    val name: String,
    val isParent: Boolean,
    val children: List<CategoryItem> = emptyList(),
    var isExpanded: Boolean = false
)
