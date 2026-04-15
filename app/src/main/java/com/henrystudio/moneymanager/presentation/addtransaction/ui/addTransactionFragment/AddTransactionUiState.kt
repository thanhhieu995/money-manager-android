package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldUiState
import java.time.LocalDate

data class AddTransactionUiState(
    val amountRaw: FieldUiState = FieldUiState(),
    val amountFormatted: String = "",
    val category: CategorySelectionUiState = CategorySelectionUiState(),
    val account: FieldUiState = FieldUiState(),
    val note: FieldUiState = FieldUiState(),
    val date: LocalDate = LocalDate.now(),
    val isIncome: Boolean = false,
    val isDirty: Boolean = false,
    val isContinueVisible: Boolean = true,
    val noteSuggestions: List<String> = emptyList(),
    val existingTransaction: Transaction? = null
)

data class CategorySelectionUiState(
    val parent: FieldUiState = FieldUiState(),
    val child: FieldUiState = FieldUiState()
)
