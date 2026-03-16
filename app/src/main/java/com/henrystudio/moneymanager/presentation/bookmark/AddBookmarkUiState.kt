package com.henrystudio.moneymanager.presentation.bookmark

import com.henrystudio.moneymanager.data.model.Transaction

data class AddBookmarkUiState(
    val transactions: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true
)
