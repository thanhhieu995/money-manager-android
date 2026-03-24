package com.henrystudio.moneymanager.presentation.addtransaction.ui.categoryDetailFragment

import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem

data class CategoryDetailUiState(
    val categoryItems: List<CategoryItem> = emptyList()
)
