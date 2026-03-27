package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldUiState
import com.henrystudio.moneymanager.presentation.addtransaction.model.SaveResult

data class AddTransactionUiState(
    val amountRaw: FieldUiState = FieldUiState(),
    val amountFormatted: String = "",
    val category: FieldUiState = FieldUiState(),
    val account: FieldUiState = FieldUiState(),
    val note: FieldUiState = FieldUiState(),
    val date: String = "",
    val isIncome: Boolean = false,
    val isEditMode: Boolean = false,
    val isContinueVisible: Boolean = true,
    val noteSuggestions: List<String> = emptyList(),
    val existingTransaction: Transaction? = null
)
