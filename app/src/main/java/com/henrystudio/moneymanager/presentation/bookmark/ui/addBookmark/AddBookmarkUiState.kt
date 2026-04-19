package com.henrystudio.moneymanager.presentation.bookmark.ui.addBookmark

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.UiState

data class AddBookmarkUiState(
    val state: UiState<List<Transaction>> = UiState.Loading,
)
