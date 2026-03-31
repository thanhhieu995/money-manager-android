package com.henrystudio.moneymanager.presentation.bookmark

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState

data class AddBookmarkUiState(
    val transactions: List<Transaction> = emptyList(),
    val dataTransactionGroupState: UiState<List<Transaction>> = UiState.Loading,
)
