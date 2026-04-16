package com.henrystudio.moneymanager.presentation.bookmark

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState

data class AddBookmarkUiState(
    val state: UiState<List<Transaction>> = UiState.Loading,
)
