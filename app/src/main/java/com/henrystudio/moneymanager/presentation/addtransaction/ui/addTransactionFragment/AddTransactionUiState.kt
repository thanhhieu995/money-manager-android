package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import com.henrystudio.moneymanager.presentation.addtransaction.model.SaveResult

data class AddTransactionUiState(
    val amountRaw: String = "",
    val amountFormatted: String = "",
    val category: String = "",
    val account: String = "",
    val note: String = "",
    val date: String = "",
    val isIncome: Boolean = false,
    val isEditMode: Boolean = false,
    val noteSuggestions: List<String> = emptyList(),
    val saveResult: SaveResult? = null
)
