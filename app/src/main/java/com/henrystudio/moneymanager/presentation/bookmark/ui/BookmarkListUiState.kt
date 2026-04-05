package com.henrystudio.moneymanager.presentation.bookmark.ui

import com.henrystudio.moneymanager.data.model.Transaction

data class BookmarkListUiState(
    val bookmarks: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true
)
