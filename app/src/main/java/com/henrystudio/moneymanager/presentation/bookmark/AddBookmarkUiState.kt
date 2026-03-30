package com.henrystudio.moneymanager.presentation.bookmark

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.views.daily.DataTransactionGroupState

data class AddBookmarkUiState(
    val transactions: List<Transaction> = emptyList(),
    val dataTransactionGroupState: DataTransactionGroupState<List<Transaction>> = DataTransactionGroupState.Loading,
)
