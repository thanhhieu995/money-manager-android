package com.henrystudio.moneymanager.presentation.views.bookmark

import com.henrystudio.moneymanager.data.model.Transaction

data class BookmarkListUiState(
    val bookmarks: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true
)
