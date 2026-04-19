package com.henrystudio.moneymanager.presentation.bookmark.ui.bookmarkList

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.presentation.bookmark.model.BookmarkItemUi

data class BookmarkListUiState(
    val bookmarks: List<BookmarkItemUi> = emptyList(),
    val categories: List<Category> = emptyList(),
)
