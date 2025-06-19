package com.example.moneymanager.ui.addtransaction

sealed class DisplayedItem {
    data class Parent(val category: CategoryItem) : DisplayedItem()
    data class ChildGroup(val children: List<CategoryItem>) : DisplayedItem()
}
