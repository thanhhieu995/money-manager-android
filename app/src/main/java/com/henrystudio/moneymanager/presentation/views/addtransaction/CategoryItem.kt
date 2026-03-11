package com.henrystudio.moneymanager.presentation.views.addtransaction

data class CategoryItem(
    val id: Int = -1,
    val emoji: String,
    val name: String,
    val parentId: Int = -1,
    val parentEmoji: String? = null,
    val parentName: String? = null,
    val isParent: Boolean,
    val children: List<CategoryItem> = emptyList(),
    var isExpanded: Boolean = false
): java.io.Serializable
